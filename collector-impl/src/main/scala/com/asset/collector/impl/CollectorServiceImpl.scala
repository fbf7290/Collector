package com.asset.collector.impl

import java.util.Calendar

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.util.Timeout
import com.asset.collector.api.{CollectorService, Country, Market, NaverEtfListResponse, Price, Stock}
import com.asset.collector.impl.repo.stock.{StockRepo, StockRepoAccessor}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import org.jsoup.Jsoup
import play.api.libs.ws.WSClient
import yahoofinance.YahooFinance
import yahoofinance.histquotes.Interval

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import cats.instances.future._
import com.asset.collector.api.Exception.ExternalResourceException
import com.asset.collector.api.Market.Market
import com.asset.collector.impl.acl.External
import com.lightbend.lagom.internal.persistence.cluster.ClusterStartupTask
import play.api.libs.json.Json
import akka.actor.typed.scaladsl.adapter._
import com.asset.collector.impl.actor.BatchActor
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration._


class CollectorServiceImpl(val system: ActorSystem, val wsClient: WSClient, val cassandraSession: CassandraSession)
      (implicit protected val  ec: ExecutionContext, implicit protected val timeout:Timeout) extends CollectorService{

  implicit val wsC = wsClient
  implicit  val typedSystem = system.toTyped

  val stockDb = StockRepo(cassandraSession)

  val batchActor = system.spawn(BatchActor(stockDb), "batchActor")

  ClusterStartupTask(system, "Init", ()=> {
    (StockRepoAccessor.createStockTable(Country.KOREA).run(stockDb) zip
     StockRepoAccessor.createStockTable(Country.USA).run(stockDb) zip
      StockRepoAccessor.createPriceTable(Country.KOREA).run(stockDb) zip
      StockRepoAccessor.createPriceTable(Country.USA).run(stockDb)).map(_=>Done)
  }, 60.seconds, None, 3.seconds, 30.seconds, 0.2)

  override def getKoreaPrice: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      val pattern1 = new scala.util.matching.Regex("<item data=\\\"(.*)\\\" />")
      External.requestKoreaStockPrice("005930", 3).map{
        a =>
          println(a)
          (ResponseHeader.Ok.withStatus(200),Done)
      }
//      wsClient.url("https://fchart.stock.naver.com/sise.nhn?timeframe=day&count=10000000&requestType=0&symbol=005930").get().map{
//        response =>
//          val result = pattern1.findAllIn(response.body).matchData.map(_.group(1)).toList
//          result.map{
//            s =>
//              val a = s.split('|')
//              if(a.size==6) println(Price("005930", a(0).toInt, a(4).toInt, a(1).toInt, a(2).toInt, a(3).toInt, a(5).toLong))
//          }
//
//          (ResponseHeader.Ok.withStatus(200),Done)
//      }
    }

  override def getUsaPrice: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      val to = Calendar.getInstance();
      val from = Calendar.getInstance()
      from.add(Calendar.YEAR, -5)
      // ^KS11(코스피), ^KQ11(코스닥), ^IXIC(나스닥), ^DJI(다우존스), ^GSPC(S&P500),
//      println(YahooFinance.get("USDKRW=X", from, to, Interval.DAILY))
//      println(YahooFinance.get("QQQ", from, to, Interval.DAILY).getDividendHistory)
      println(YahooFinance.get("CLR", from, to, Interval.DAILY).getDividendHistory)
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
      wsClient.url("http://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13&marketType=kospiMkt").get().map{
        response =>
          val a = Jsoup.parseBodyFragment(response.body).body().getElementsByTag("tr")
          for(d <- a.asScala){
            val c = d.getElementsByTag("td").asScala
//            if(c.size != 0) println(s"${c(0).text} ${c(1).text}")
            for(b <- c){
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

  override def storeKoreaStock: ServiceCall[NotUsed, Done] = ServerServiceCall{ (_, _) =>
    Future.successful(ResponseHeader.Ok.withStatus(200),Done)
  }

  override def getKoreaEtfStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall{ (_, _) =>
    External.requestKoreaEtfStockList.map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def getKospiStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestKoreaMarketStockList(Market.KOSPI).map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def getKosdaqStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestKoreaMarketStockList(Market.KOSDAQ).map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def requestBatchKoreaStock: ServiceCall[NotUsed, Done] = ServerServiceCall { (_, _) =>
    batchActor.ask[BatchActor.Reply.type](reply => BatchActor.CollectKoreaStock(Some(reply)))
      .map(_=>(ResponseHeader.Ok.withStatus(200),Done))
  }

  override def getNasdaqStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestUsaMarketStockList(Market.NASDAQ).map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def getNyseStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestUsaMarketStockList(Market.NYSE).map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def getAmexStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestUsaMarketStockList(Market.AMEX).map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def requestBatchUsaStock: ServiceCall[NotUsed, Done] = ServerServiceCall { (_, _) =>
    batchActor.ask[BatchActor.Reply.type](reply => BatchActor.CollectUsaStock(Some(reply)))
      .map(_=>(ResponseHeader.Ok.withStatus(200),Done))
  }
}
