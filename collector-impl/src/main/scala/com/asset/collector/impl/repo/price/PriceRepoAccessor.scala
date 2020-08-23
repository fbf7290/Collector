package com.asset.collector.impl.repo.price

import akka.Done
import cats.Monad
import cats.data.ReaderT
import com.asset.collector.api.Country.Country
import com.asset.collector.api.Price

object PriceRepoAccessor {

  def createPriceTable[F[_]:Monad](country:Country):ReaderT[F, PriceRepoTrait[F], Done] =
    ReaderT[F, PriceRepoTrait[F], Done] {
      db => db.createPriceTable(country)
    }

  def selectLatestTimestamp[F[_]:Monad](country:Country, code:String):ReaderT[F, PriceRepoTrait[F], Long] =
    ReaderT[F, PriceRepoTrait[F], Long] {
      db => db.selectLatestTimestamp(country, code)
    }

  def insertPrice[F[_]:Monad](country:Country, price:Price):ReaderT[F, PriceRepoTrait[F], Done] =
    ReaderT[F, PriceRepoTrait[F], Done] {
      db => db.insertPrice(country, price)
    }
}
