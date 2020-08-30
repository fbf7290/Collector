package com.asset.collector.api

import akka.{Done, NotUsed}
import com.asset.collector.api.Market.Market
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.Environment

trait CollectorService extends Service{

  def getKoreaPrice: ServiceCall[NotUsed, Done]
  def getUsaPrice: ServiceCall[NotUsed, Done]
  def getKoreaEtfList: ServiceCall[NotUsed, Done]
  def getKoreaStockList: ServiceCall[NotUsed, Done]
  def getUsaStockList: ServiceCall[NotUsed, Done]

  def storeKoreaStock: ServiceCall[NotUsed, Done]
  def getKoreaEtfStockList: ServiceCall[NotUsed, Seq[Stock]]
  def getKospiStockList: ServiceCall[NotUsed, Seq[Stock]]
  def getKosdaqStockList: ServiceCall[NotUsed, Seq[Stock]]

  def requestBatchKoreaStock: ServiceCall[NotUsed, Done]


  override def descriptor: Descriptor ={
    import Service._

    named("Collector")
      .withCalls(
        restCall(Method.GET, "/price/korea", getKoreaPrice),
        restCall(Method.GET, "/price/usa", getUsaPrice),
        restCall(Method.GET, "/etf/korea", getKoreaEtfList),
        restCall(Method.GET, "/stock/korea", getKoreaStockList),
        restCall(Method.GET, "/stock/usa", getUsaStockList),
        restCall(Method.GET, "/stock/korea1", storeKoreaStock),

        restCall(Method.GET, "/stock/korea/etf/stockList", getKoreaEtfStockList),
        restCall(Method.GET, "/stock/korea/kospi/stockList", getKospiStockList),
        restCall(Method.GET, "/stock/korea/kosdaq/stockList", getKosdaqStockList),

        restCall(Method.POST, "/stock/korea/batch", requestBatchKoreaStock)
      ).withAutoAcl(true)
      .withExceptionSerializer(new ClientExceptionSerializer(Environment.simple()))

  }
}
