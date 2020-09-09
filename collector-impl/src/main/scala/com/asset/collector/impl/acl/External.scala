package com.asset.collector.impl.acl

import java.text.SimpleDateFormat
import java.util.Calendar

import com.asset.collector.api.Exception.ExternalResourceException
import com.asset.collector.api.Market.Market
import com.asset.collector.api.{DumbStock, Market, NaverEtfListResponse, Price, Stock}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import yahoofinance.YahooFinance
import yahoofinance.histquotes.Interval

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

object External {
//TODO price의 int를 float로 바꾼다.
  def requestKoreaEtfStockList(implicit wsClient: WSClient, ec: ExecutionContext):Future[Seq[Stock]] = {
    var stockList = ListBuffer.empty[Stock]
    wsClient.url("https://finance.naver.com/api/sise/etfItemList.nhn").get().map{
      response =>
        val naverEtfListResponse = Json.parse(response.body).as[NaverEtfListResponse]
        (naverEtfListResponse.resultCode=="success") match {
          case true =>
            stockList ++= naverEtfListResponse.result.etfItemList.map(etf => Stock(Market.KOSPI, etf.itemname, etf.itemcode))
            stockList.toSeq
          case false => throw ExternalResourceException
        }
    }
  }


  def requestKoreaMarketStockList(market: Market)(implicit wsClient: WSClient, ec: ExecutionContext):Future[Seq[Stock]]= {
    var stockList = ListBuffer.empty[Stock]
    val marketParam = if(market == Market.KOSDAQ) "kosdaqMkt" else if(market == Market.KOSPI) "stockMkt"
    wsClient.url(s"http://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13&marketType=${marketParam}").get().map{
      response =>
        val stocks = Jsoup.parseBodyFragment(response.body).body().getElementsByTag("tr")
        for(stock <- stocks.asScala){
          val stockAttrs = stock.getElementsByTag("td").asScala
          if(stockAttrs.size != 0) stockList += Stock(market, stockAttrs(0).text, stockAttrs(1).text)
        }
        stockList.toList
    }.recover{case _ => throw ExternalResourceException}
  }

  def requestUsaMarketStockList(market:Market)(implicit wsClient: WSClient, ec: ExecutionContext):Future[Seq[Stock]] = {
    var stockList = ListBuffer.empty[Stock]
    val marketParam = market match {
      case Market.NASDAQ => "NASDAQ"
      case Market.NYSE => "NYSE"
      case Market.AMEX => "AMEX"
    }
    wsClient.url(s"https://dumbstockapi.com/stock?exchanges=${marketParam}").get.map{
      response =>
        Json.parse(response.body).as[Seq[DumbStock]].foreach(dumbStock => stockList += Stock(market, dumbStock.name, dumbStock.ticker.replace("^", "-P")))
        println(response.body)
        stockList.toSeq
    }.recover{case _ => throw ExternalResourceException}
  }

  def requestKoreaStockPrice(code:String, count:Int=Int.MaxValue)(implicit wsClient: WSClient, ec: ExecutionContext):Future[Seq[Price]] =
    wsClient.url(s"https://fchart.stock.naver.com/sise.nhn?timeframe=day&count=${count}&requestType=0&symbol=${code}").get().map{
      response =>
        val pattern = new scala.util.matching.Regex("<item data=\\\"(.*)\\\" />")
        pattern.findAllIn(response.body).matchData.map(_.group(1).split('|')).toList.filter(_.size==6)
          .map(arr => Price(code, arr(0), arr(4), arr(1), arr(2), arr(3), arr(5))).toSeq
    }

  def requestUsaStockPrice(code:String, year:Int=30)(implicit ec: ExecutionContext):Future[Seq[Price]] =
    Future{
      val from = Calendar.getInstance()
      from.add(Calendar.YEAR, -1*year)
      val format = new SimpleDateFormat("yyyyMMdd")
      YahooFinance.get(code, from, Interval.DAILY).getHistory.asScala.map{
        stock =>
          Price(code, format.format(stock.getDate.getTime()), stock.getClose.toString, stock.getOpen.toString, stock.getHigh.toString, stock.getLow.toString, stock.getVolume.toString)
      }
    }
}
