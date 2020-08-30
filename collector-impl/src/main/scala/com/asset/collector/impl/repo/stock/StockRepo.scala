package com.asset.collector.impl.repo.stock

import akka.Done
import com.asset.collector.api.Country.Country
import com.asset.collector.api.{Market, Price, Stock}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.datastax.driver.core.BatchStatement

import scala.concurrent.{ExecutionContext, Future}



case class StockRepo(session: CassandraSession)(implicit val  ec: ExecutionContext) extends StockRepoTrait[Future]{

  override def createStockTable(country: Country): Future[Done] =
    session.executeCreateTable(s"create table if not exists ${country}_stock (ignored TEXT, code TEXT, name TEXT, market TEXT, PRIMARY KEY(ignored, code))")

  override def selectStocks(country: Country): Future[Seq[Stock]] =
    session.selectAll(s"select code, name, market from ${country}_stock").map{ rows =>
      rows.map(row => Stock(Market.toMarket(row.getString("market")).get, row.getString("name"), row.getString("code")))
    }

  override def insertStock(country: Country, stock:Stock): Future[Done] =
    session.executeWrite(s"INSERT INTO ${country}_stock (ignored, code, name, market) VALUES ('1', '${stock.code}', '${stock.name}', '${stock.market}')")

  override def insertBatchStock(country: Country, stocks: Seq[Stock]): Future[Done] =
    for {
      stmt <- session.prepare(s"INSERT INTO ${country}_stock (ignored, code, name, market) VALUES ('1', ?, ?, ?)")
      batch = new BatchStatement
      _ = stocks.map { stock =>
        batch.add(stmt.bind
        .setString("code", stock.code)
        .setString("name", stock.name)
        .setString("market", stock.market.toString))
      }
      r <- session.executeWriteBatch(batch)
    } yield {
      r
    }

  override def deleteStock(country: Country, stock: Stock): Future[Done] =
    session.executeWrite(s"DELETE FROM ${country}_stock where ignored='1' and code='${stock.code}'")

  override def createPriceTable(country: Country): Future[Done] =
    session.executeCreateTable(s"create table if not exists ${country}_price (code TEXT, date INT, close INT, open INT, low INT, high INT, volume BIGINT, PRIMARY KEY(code, date)) WITH CLUSTERING ORDER BY (date DESC)")

  override def selectLatestTimestamp(country: Country, code: String): Future[Option[Int]] =
    session.selectOne(s"select date from ${country}_price where code='${code}").map(_.map(_.getInt("data")))

  override def insertPrice(country:Country, price: Price): Future[Done] =
    session.executeWrite(s"INSERT INTO ${country}_price (code, date, close, open, low, high, volume) VALUES ('${price.code}', ${price.date}, ${price.close}, ${price.open}, ${price.low}, ${price.high}, ${price.volume}")

  override def insertBatchPrice(country: Country, prices: Seq[Price]): Future[Done] =
    for {
      stmt <- session.prepare(s"INSERT INTO ${country}_price (code, date, close, open, low, high, volume) VALUES (?, ?, ?, ?, ?, ?, ?)")
      batch = new BatchStatement
      _ = prices.map { price =>
        batch.add(stmt.bind
          .setString("code", price.code)
          .setInt("date", price.date)
          .setInt("close", price.close)
          .setInt("open", price.close)
          .setInt("low", price.close)
          .setInt("high", price.close)
          .setLong("volume", price.close))
      }
      r <- session.executeWriteBatch(batch)
    } yield {
      r
    }
}