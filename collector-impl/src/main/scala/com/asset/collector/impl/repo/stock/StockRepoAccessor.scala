package com.asset.collector.impl.repo.stock

import akka.Done
import cats.Monad
import cats.data.ReaderT
import com.asset.collector.api.Country.Country
import com.asset.collector.api.Market.Market
import com.asset.collector.api.{NaverStockIndex09, Price, Stock}

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

  def insertBatchStock[F[_]:Monad](country: Country, stocks: Seq[Stock]):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.insertBatchStock(country, stocks)
    }

  def deleteStock[F[_]:Monad](country: Country, stock:Stock):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.deleteStock(country, stock)
    }


  def createPriceTable[F[_]:Monad](country:Country):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.createPriceTable(country)
    }

  def selectLatestTimestamp[F[_]:Monad](country:Country, code:String):ReaderT[F, StockRepoTrait[F], Option[String]] =
    ReaderT[F, StockRepoTrait[F], Option[String]] {
      db => db.selectLatestTimestamp(country, code)
    }

  def insertPrice[F[_]:Monad](country:Country, price:Price):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.insertPrice(country, price)
    }

  def insertBatchPrice[F[_]:Monad](country:Country, prices:Seq[Price]):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.insertBatchPrice(country, prices)
    }

  def createStockIndex09Table[F[_]:Monad]:ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.createStockIndex09Table
    }
  def insertBatchStockIndex09[F[_]:Monad](indexes:Seq[NaverStockIndex09]):ReaderT[F, StockRepoTrait[F], Done] =
    ReaderT[F, StockRepoTrait[F], Done] {
      db => db.insertBatchStockIndex09(indexes)
    }
  def selectStockIndex09[F[_]:Monad](code:String, category:String):ReaderT[F, StockRepoTrait[F], Option[NaverStockIndex09]] =
    ReaderT[F, StockRepoTrait[F], Option[NaverStockIndex09]] {
      db => db.selectStockIndex09(code, category)
    }


}
