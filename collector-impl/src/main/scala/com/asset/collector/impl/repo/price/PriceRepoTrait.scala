package com.asset.collector.impl.repo.price

import akka.Done
import com.asset.collector.api.Country.Country
import com.asset.collector.api.Price

trait PriceRepoTrait[F[_]] {
  def createPriceTable(country:Country):F[Done]
  def selectLatestTimestamp(country: Country, code: String):F[Long]
  def insertPrice(country:Country, price:Price):F[Done]
}
