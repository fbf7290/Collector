package com.asset.collector.impl.actor

import java.time.temporal.ChronoUnit
import java.time.{ZoneId, ZonedDateTime}

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.asset.collector.api.{Country, Market, Stock}
import com.asset.collector.impl.repo.stock.{StockRepoAccessor, StockRepoTrait}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import cats.instances.future._
import com.asset.collector.api.Country.Country
import com.asset.collector.impl.acl.External

import scala.util.{Failure, Success}

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

          setDailyTimer(CollectKoreaStock(None), 0)
          setDailyTimer(CollectUsaStock(None), 8)

          def ing:Behavior[Command] = Behaviors.receiveMessage {
            case CollectKoreaStock(replyTo) =>
              replyTo.map(_.tell(Reply))
              context.pipeToSelf {
                for {
                  stocks <- StockRepoAccessor.selectStocks(Country.KOREA).run(stockDb).map(_.toSet)
                  nowStocksList <- Future.sequence(List(External.requestKoreaEtfStockList,
                    External.requestKoreaMarketStockList(Market.KOSPI),
                    External.requestKoreaMarketStockList(Market.KOSDAQ)))
                  nowStocks = nowStocksList.foldLeft(Set.empty[Stock])((r, stocks) => r ++ stocks.toSet)
                  _ <- StockRepoAccessor.insertBatchStock(Country.KOREA, (nowStocks -- stocks).toSeq).run(stockDb)
                  _ <- Future.sequence((stocks -- nowStocks).map(stock => StockRepoAccessor.deleteStock(Country.KOREA, stock).run(stockDb)))
                } yield {}
              }{
                case Success(_) => SuccessBatch(Country.KOREA)
                case Failure(exception) => throw exception
              }
              stash
            case CollectUsaStock(replyTo) =>
              replyTo.map(_.tell(Reply))
              Behaviors.same
            case _ =>
              Behaviors.same
          }

          def stash:Behavior[Command] = Behaviors.receiveMessage {
            case SuccessBatch(country) =>
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
