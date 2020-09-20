package com.asset.collector.impl

import java.util.Calendar

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.util.Timeout
import com.asset.collector.api.{CollectorService, CollectorSettings, Country, Market, NaverEtfListResponse, NaverStockIndex09, Price, Stock, Test}
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
import play.api.libs.json.{JsNull, Json}
import akka.actor.typed.scaladsl.adapter._
import com.asset.collector.impl.actor.BatchActor
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.asset.collector.impl.Fcm.FcmMessage

import scala.concurrent.duration._
import scala.util.{Failure, Success}


class CollectorServiceImpl(val system: ActorSystem, val wsClient: WSClient, val cassandraSession: CassandraSession)
      (implicit ec: ExecutionContext, timeout:Timeout,  materializer: Materializer) extends CollectorService {

  implicit val wsC = wsClient
  implicit val typedSystem = system.toTyped

  val stockDb = StockRepo(cassandraSession)

  val batchActor = system.spawn(BatchActor(stockDb), "batchActor")

  ClusterStartupTask(system, "Init", () => {
    (StockRepoAccessor.createStockTable(Country.KOREA).run(stockDb) zip
      StockRepoAccessor.createStockTable(Country.USA).run(stockDb) zip
      StockRepoAccessor.createPriceTable(Country.KOREA).run(stockDb) zip
      StockRepoAccessor.createStockIndex09Table.run(stockDb) zip
      StockRepoAccessor.createPriceTable(Country.USA).run(stockDb)).map(_ => Done)
  }, 60.seconds, None, 3.seconds, 30.seconds, 0.2)

  override def test: ServiceCall[Test, Done] = ServerServiceCall{
    (_, a) =>
      println(a)
      Future.successful(ResponseHeader.Ok.withStatus(200), Done)
  }


  override def getUsaPrice: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      val to = Calendar.getInstance();
      val from = Calendar.getInstance()
      from.add(Calendar.YEAR, -10)
      // ^KS11(코스피), ^KQ11(코스닥), ^IXIC(나스닥), ^DJI(다우존스), ^GSPC(S&P500),
      //      println(YahooFinance.get("USDKRW=X", from, to, Interval.DAILY))
      //      println(YahooFinance.get("QQQ", from, to, Interval.DAILY).getDividendHistory)
//      println(YahooFinance.get("005930.KS", from, Interval.DAILY).getHistory.get(0).getAdjClose)

      YahooFinance.get("005930.KS", from, Interval.DAILY).getHistory.asScala.foreach{
        st => println(s"${st.getDate}_${st.getClose}_${st.getAdjClose}")
      }

      Future.successful(ResponseHeader.Ok.withStatus(200), Done)
    }

  override def getKoreaEtfList: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      wsClient.url("https://finance.naver.com/api/sise/etfItemList.nhn").get().map {
        response =>
          println(response.body)
          (ResponseHeader.Ok.withStatus(200), Done)
      }
    }

  override def getKoreaStockList: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      wsClient.url("http://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13&marketType=kospiMkt").get().map {
        response =>
          val a = Jsoup.parseBodyFragment(response.body).body().getElementsByTag("tr")
          for (d <- a.asScala) {
            val c = d.getElementsByTag("td").asScala
            //            if(c.size != 0) println(s"${c(0).text} ${c(1).text}")
            for (b <- c) {
              println(b.text())
            }
          }
          (ResponseHeader.Ok.withStatus(200), Done)
      }
    }


  override def getUsaStockList: ServiceCall[NotUsed, Done] =
    ServerServiceCall { (requestHeader, _) =>
      wsClient.url("https://dumbstockapi.com/stock?exchanges=NYSE").get.map {
        response =>
          println(response.body)
          (ResponseHeader.Ok.withStatus(200), Done)
      }
    }

  override def getKoreaEtfStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestKoreaEtfStockList.map { stockList =>
      (ResponseHeader.Ok.withStatus(200), stockList)
    }
  }

  override def getKospiStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestKoreaMarketStockList(Market.KOSPI).map { stockList =>
      (ResponseHeader.Ok.withStatus(200), stockList)
    }
  }

  override def getKosdaqStockList: ServiceCall[NotUsed, Seq[Stock]] = ServerServiceCall { (_, _) =>
    External.requestKoreaMarketStockList(Market.KOSDAQ).map { stockList =>
      (ResponseHeader.Ok.withStatus(200), stockList)
    }
  }

  override def getKoreaStockPrices(code:String): ServiceCall[NotUsed, Seq[Price]] = ServerServiceCall { (_, _) =>
    External.requestKoreaStockPrice(code).map { priceList =>
        (ResponseHeader.Ok.withStatus(200), priceList)
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

  override def getUsaStockPrices(code: String): ServiceCall[NotUsed, Seq[Price]] = ServerServiceCall { (_, _) =>
    External.requestUsaStockPrice(code).map{ stockList =>
      (ResponseHeader.Ok.withStatus(200),stockList)
    }
  }

  override def requestBatchUsaStock: ServiceCall[NotUsed, Done] = ServerServiceCall { (_, _) =>
    batchActor.ask[BatchActor.Reply.type](reply => BatchActor.CollectUsaStock(Some(reply)))
      .map(_=>(ResponseHeader.Ok.withStatus(200),Done))
  }

  override def collectNaverStockIndexes: ServiceCall[NotUsed, Done] = ServerServiceCall{ (_,_)=>

    for {
      stockList <- (External.requestKoreaMarketStockList(Market.KOSPI) zip External.requestKoreaMarketStockList(Market.KOSDAQ)).map(r=>r._2++r._1)
    }yield{
      val failList = ListBuffer.empty[String]
      Source(stockList.toList.zipWithIndex).mapAsync(4){ case (stock, index) =>
        println(index)
        println(stock.code)
        wsClient.url(s"https://finance.naver.com/item/main.nhn?code=${stock.code}").get()
          .flatMap{ response =>
            var datas = ListBuffer.empty[NaverStockIndex09]
            for(i <- List(2,6,7,8,12,13,14,15)){
              val category = Jsoup.parseBodyFragment(response.body).body().select(s"table.tb_type1_ifrs > tbody > tr:eq(${i}) > th").text.trim
              val indexes = Jsoup.parseBodyFragment(response.body).body().select(s"table.tb_type1_ifrs > tbody > tr:eq(${i}) > td").asScala.toList.map(_.text.filterNot(_==' ').trim)
              datas += NaverStockIndex09(stock.code, category, indexes:_*)
            }
            StockRepoAccessor.insertBatchStockIndex09(datas.toList).run(stockDb)
          }.recover{case e =>
          println(e)
          failList += stock.code}
      }.runWith(Sink.ignore).onComplete{
        case Success(_) =>
          println(failList)
          Fcm.sendFcmMsg(List(CollectorSettings.adminFcmRegistrationId), FcmMessage("지표 batch 성공", failList.toString, JsNull))
        case Failure(exception) => Fcm.sendFcmMsg(List(CollectorSettings.adminFcmRegistrationId), FcmMessage("지표 batch 실패", exception.getMessage, JsNull))
      }
    }

    Future.successful((ResponseHeader.Ok.withStatus(200),Done))
  }
}
