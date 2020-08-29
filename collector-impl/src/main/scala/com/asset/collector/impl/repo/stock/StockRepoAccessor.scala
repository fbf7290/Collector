package com.asset.collector.impl.repo.stock

import akka.Done
import cats.Monad
import cats.data.ReaderT
import com.asset.collector.api.Country.Country
import com.asset.collector.api.Market.Market
import com.asset.collector.api.Stock

object StockRepoAccessor {

  def createStockTable[F[_]:Monad](country:Country):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.createStockTable(country)
    }

  def selectStocks[F[_]:Monad](country:Country):ReaderT[F, StockRepoTrait[F], Seq[Stock]] =
    ReaderT[F, StockRepoTrait[F], Seq[Stock]] {
      db => db.selectStocks(country)
    }

  def insertStock[F[_]:Monad](country: Country, stock:Stock):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.insertStock(country, stock)
    }
}
