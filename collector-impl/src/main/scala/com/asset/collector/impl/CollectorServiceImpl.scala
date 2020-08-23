package com.asset.collector.impl

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL
import java.util.Calendar

import akka.{Done, NotUsed}
import akka.util.Timeout
import com.asset.collector.api.CollectorService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import org.jsoup.Jsoup
import play.api.libs.ws.WSClient
import yahoofinance.{Stock, YahooFinance}
import yahoofinance.histquotes.Interval

import collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

class CollectorServiceImpl(val wsClient: WSClient)
      (implicit protected val  ec: ExecutionContext, implicit protected val timeout:Timeout) extends CollectorService{

  override def getKoreaPrice: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      val pattern1 = new scala.util.matching.Regex("<item data=\\\"(.*)\\\" />")

      wsClient.url("https://fchart.stock.naver.com/sise.nhn?timeframe=day&count=10000000&requestType=0&symbol=005930").get().map{
        response =>
          val result = pattern1.findAllIn(response.body).matchData.map(_.group(1)).toList
          println(result)
          (ResponseHeader.Ok.withStatus(200),Done)
      }
    }

  override def getUsaPrice: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      val to = Calendar.getInstance();
      val from = Calendar.getInstance()
      from.add(Calendar.YEAR, -5)
      // ^KS11(코스피), ^KQ11(코스닥), ^IXIC(나스닥), ^DJI(다우존스), ^GSPC(S&P500),
      println(YahooFinance.get("USDKRW=X", from, to, Interval.DAILY))
      Future.successful(ResponseHeader.Ok.withStatus(200),Done)
    }

  override def getKoreaEtfList: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>

      wsClient.url("https://finance.naver.com/api/sise/etfItemList.nhn").get().map{
        response =>
          println(response.body)
          (ResponseHeader.Ok.withStatus(200),Done)
      }
    }

  override def getKoreaStockList: ServiceCall[NotUsed, Done] =

    ServerServiceCall { (requestHeader, _) =>
//http://old.nasdaq.com/screening/companies-by-name.aspx?letter=0&render=download&exchange=NYSE  AMEX  NASDAQ
      wsClient.url("http://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13&marketType=kosdaqMkt").get().map{
        response =>
          val a = Jsoup.parseBodyFragment(response.body).body().getElementsByTag("tr")
          for(d <- a.asScala){
            val c = d.getElementsByTag("td")
            for(b <- c.asScala){
              println(b.text())
            }
          }

          (ResponseHeader.Ok.withStatus(200),Done)
      }
    }

  override def getUsaStockList: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>

      wsClient.url("https://dumbstockapi.com/stock?exchanges=NYSE").get.map{
        response =>
          println(response.body)
          (ResponseHeader.Ok.withStatus(200),Done)
      }
    }
}
