package com.asset.collector.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.Environment

trait CollectorService extends Service{

  def getKoreaPrice: ServiceCall[NotUsed, Done]
  def getUsaPrice: ServiceCall[NotUsed, Done]
  def getKoreaEtfList: ServiceCall[NotUsed, Done]
  def getKoreaStockList: ServiceCall[NotUsed, Done]
  def getUsaStockList: ServiceCall[NotUsed, Done]

  override def descriptor: Descriptor ={
    import Service._

    named("Collector")
      .withCalls(
        restCall(Method.GET, "/price/korea", getKoreaPrice),
        restCall(Method.GET, "/price/usa", getUsaPrice),
        restCall(Method.GET, "/etf/korea", getKoreaEtfList),
        restCall(Method.GET, "/stock/korea", getKoreaStockList),
        restCall(Method.GET, "/stock/usa", getUsaStockList),
      ).withAutoAcl(true)
      .withExceptionSerializer(new ClientExceptionSerializer(Environment.simple()))

  }
}
