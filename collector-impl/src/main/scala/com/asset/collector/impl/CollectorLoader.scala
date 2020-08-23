package com.asset.collector.impl

import akka.util.Timeout
import com.asset.collector.api.CollectorService
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire.wire
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

import scala.concurrent.duration._

class CollectorLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
//    new CollectorApplication(context) with ConfigurationServiceLocatorComponents
    new CollectorApplication(context) with AkkaDiscoveryComponents


  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CollectorApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CollectorService])
}

abstract class CollectorApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  implicit lazy val timeout:Timeout = Timeout(5.seconds)

  override lazy val lagomServer = serverFor[CollectorService](wire[CollectorServiceImpl])


  val serializers1 : Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CreatingUser]
  )

  case class CreatingUser(userId:String, pwd:String, department:String)
  object CreatingUser {
    implicit val format: Format[CreatingUser] = Json.format
  }

  override lazy val jsonSerializerRegistry = new JsonSerializerRegistry {
    override def serializers  = Seq(
      JsonSerializer[CreatingUser]
    )
  }
}