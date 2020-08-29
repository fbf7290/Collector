package com.asset.collector.impl.repo.stock

import akka.Done
import com.asset.collector.api.Country.Country
import com.asset.collector.api.Market.Market
import com.asset.collector.api.Stock


trait StockRepoTrait[F[_]] {
  def createStockTable(country:Country):F[Done]
  def selectStocks(country: Country):F[Seq[Stock]]
  def insertStock(country: Country, stock:Stock):F[Done]
  def insertBatchStock(country: Country, stocks:Seq[Stock]):F[Done]
}
