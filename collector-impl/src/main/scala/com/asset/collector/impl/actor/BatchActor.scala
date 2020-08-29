package com.asset.collector.impl.actor

import java.time.temporal.ChronoUnit
import java.time.{ZoneId, ZonedDateTime}

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._


object BatchActor {
  sealed trait Command
  case object CollectKoreaStock extends Command
  case object CollectUsaStock extends Command

  def apply():Behavior[Command] = Behaviors.setup{ context =>
    Behaviors.supervise[Command]{
      Behaviors.withTimers[Command]{ timers =>

        def setDailyTimer(msg:Command, plusHours:Int) = {
          val zoneId = ZoneId.of("UTC+09:00")
          val now = ZonedDateTime.now(zoneId)
          val after = now.plusDays(1).truncatedTo( ChronoUnit.DAYS ).plusHours(plusHours)
          timers.startSingleTimer(msg, msg, (after.toEpochSecond-now.toEpochSecond).seconds)
        }

        setDailyTimer(CollectKoreaStock, 0)
        setDailyTimer(CollectUsaStock, 8)

        Behaviors.receiveMessage{
          case CollectKoreaStock =>



            Behaviors.same
          case CollectUsaStock =>
            Behaviors.same
        }

      }
    }.onFailure[Exception](SupervisorStrategy.restart)
  }

}
