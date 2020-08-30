package com.asset.collector.impl.actor

import java.time.temporal.ChronoUnit
import java.time.{ZoneId, ZonedDateTime}

import akka.Done
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.asset.collector.api.{Country, Market, Stock}
import com.asset.collector.impl.repo.stock.{StockRepoAccessor, StockRepoTrait}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import com.asset.collector.api.Country.Country
import com.asset.collector.impl.acl.External
//import cats.implicits._
import scala.util.{Failure, Success}
import cats.syntax.traverse._
import cats.instances.future._
import cats.instances.list._

object BatchActor {
  sealed trait Command
  case class CollectKoreaStock(replyTo:Option[ActorRef[Reply.type]]) extends Command
  case class CollectUsaStock(replyTo:Option[ActorRef[Reply.type]]) extends Command
  case class SuccessBatch(country: Country) extends Command

  sealed trait Response
  case object Reply extends Response

  def apply(stockDb:StockRepoTrait[Future])
           (implicit wsClient: WSClient, ec: ExecutionContext):Behavior[Command] = Behaviors.setup{ context =>
    Behaviors.supervise[Command]{
      Behaviors.withStash(20){ buffer =>
        Behaviors.withTimers[Command] { timers =>

          def setDailyTimer(msg: Command, plusHours: Int) = {
            val zoneId = ZoneId.of("UTC+09:00")
            val now = ZonedDateTime.now(zoneId)
            val after = now.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(plusHours)
            timers.startSingleTimer(msg, msg, (after.toEpochSecond - now.toEpochSecond).seconds)
          }

          def refreshStockList(country: Country, nowStocks:Set[Stock]) =
            for {
              stocks <- StockRepoAccessor.selectStocks[Future](country).map(_.toSet)
              _ <- StockRepoAccessor.insertBatchStock[Future](country, (nowStocks -- stocks).toSeq)
              _ <-  (stocks -- nowStocks).map(stock => StockRepoAccessor.deleteStock[Future](country, stock)).toList.sequence
            } yield {}


          setDailyTimer(CollectKoreaStock(None), 0)
          setDailyTimer(CollectUsaStock(None), 8)


          def ing:Behavior[Command] = Behaviors.receiveMessage {
            case CollectKoreaStock(replyTo) =>
              replyTo.map(_.tell(Reply))
              context.pipeToSelf {
                for{
                  nowStocksList <- Future.sequence(Set(External.requestKoreaEtfStockList,
                    External.requestKoreaMarketStockList(Market.KOSPI),
                    External.requestKoreaMarketStockList(Market.KOSDAQ)))
                  nowStocks = nowStocksList.foldLeft(Set.empty[Stock])((r, stocks) => r ++ stocks)
                  _ <- refreshStockList(Country.KOREA, nowStocks).run(stockDb)
                } yield {}
              }{
                case Success(_) => SuccessBatch(Country.KOREA)
                case Failure(exception) => throw exception
              }
              stash
            case CollectUsaStock(replyTo) =>
              replyTo.map(_.tell(Reply))
              context.pipeToSelf {
                for{
                  nowStocksList <- Future.sequence(Set(External.requestUsaMarketStockList(Market.NASDAQ),
                    External.requestUsaMarketStockList(Market.NYSE),
                    External.requestUsaMarketStockList(Market.AMEX)))
                  nowStocks = nowStocksList.foldLeft(Set.empty[Stock])((r, stocks) => r ++ stocks)
                  _ <- refreshStockList(Country.USA, nowStocks).run(stockDb)
                } yield {}
              }{
                case Success(_) => SuccessBatch(Country.USA)
                case Failure(exception) => throw exception
              }
              stash
            case _ =>
              Behaviors.same
          }

          def stash:Behavior[Command] = Behaviors.receiveMessage {
            case SuccessBatch(country) =>
              println("finish")
              country match {
                case Country.KOREA => setDailyTimer(CollectKoreaStock(None), 0)
                case Country.USA => setDailyTimer(CollectUsaStock(None), 8)
              }
              buffer.unstashAll(ing)
            case other =>
              buffer.stash(other)
              Behaviors.same
          }
          ing
        }
      }
    }.onFailure[Exception](SupervisorStrategy.restart)
  }

}
