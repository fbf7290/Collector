package com.asset.collector.impl.repo.stock

import akka.Done
import cats.data.OptionT
import cats.instances.future._
import com.asset.collector.api.Country.Country
import com.asset.collector.api.{Market, NaverStockIndex09, Price, Stock}
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
    session.executeCreateTable(s"create table if not exists ${country}_price (code TEXT, date TEXT, close TEXT, open TEXT, low TEXT, high TEXT, volume TEXT, PRIMARY KEY(code, date)) WITH CLUSTERING ORDER BY (date DESC)")

  override def selectLatestTimestamp(country: Country, code: String): Future[Option[String]] =
    session.selectOne(s"select date from ${country}_price where code='${code}").map(_.map(_.getString("data")))

  override def insertPrice(country:Country, price: Price): Future[Done] =
    session.executeWrite(s"INSERT INTO ${country}_price (code, date, close, open, low, high, volume) VALUES ('${price.code}', ${price.date}, ${price.close}, ${price.open}, ${price.low}, ${price.high}, ${price.volume}")

  override def insertBatchPrice(country: Country, prices: Seq[Price]): Future[Done] = {
    for {
      stmt <- session.prepare(s"INSERT INTO ${country}_price (code, date, close, open, low, high, volume) VALUES (?, ?, ?, ?, ?, ?, ?)")
      batch = new BatchStatement
      _ = prices.map { price =>
        batch.add(stmt.bind
          .setString("code", price.code)
          .setString("date", price.date)
          .setString("close", price.close)
          .setString("open", price.open)
          .setString("low", price.low)
          .setString("high", price.high)
          .setString("volume", price.volume))
      }
      r <- session.executeWriteBatch(batch)
    } yield {
      r
    }
  }

  override def createStockIndex09Table: Future[Done] =
    session.executeCreateTable(s"create table if not exists stock_index_09 " +
      s"(code TEXT, category TEXT, y_index_201712 TEXT, y_index_201812 TEXT, y_index_201912 TEXT, y_index_202012e TEXT" +
      s", q_index_201906 TEXT, q_index_201909 TEXT, q_index_201912 TEXT, q_index_202003 TEXT, q_index_202006 TEXT, q_index_202009e TEXT, PRIMARY KEY(code, category))")

  override def insertBatchStockIndex09(indexes: Seq[NaverStockIndex09]): Future[Done] =
    for {
      stmt <- session.prepare(s"INSERT INTO stock_index_09 (code, category, y_index_201712, y_index_201812, y_index_201912, y_index_202012e" +
        s", q_index_201906, q_index_201909, q_index_201912, q_index_202003, q_index_202006, q_index_202009e) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")
      batch = new BatchStatement
      _ = indexes.map { index =>
        batch.add(stmt.bind
        .setString("code", index.code)
        .setString("category", index.category)
        .setString("y_index_201712", index.yIndex201712)
        .setString("y_index_201812", index.yIndex201812)
        .setString("y_index_201912", index.yIndex201912)
        .setString("y_index_202012e", index.yIndex202012E)
        .setString("q_index_201906", index.qIndex201906)
        .setString("q_index_201909", index.qIndex201909)
        .setString("q_index_201912", index.qIndex201912)
        .setString("q_index_202003", index.qIndex202003)
        .setString("q_index_202006", index.qIndex202006)
        .setString("q_index_202009e", index.qIndex202009E))
      }
      r <- session.executeWriteBatch(batch)
    } yield {
      r
    }

  override def selectStockIndex09(code: String, category: String): Future[Option[NaverStockIndex09]] =
    OptionT(session.selectOne(s"select y_index_201712, y_index_201812, y_index_201912, y_index_202012e" +
      s", q_index_201906, q_index_201909, q_index_201912, q_index_202003, q_index_202006, q_index_202009e" +
      s" from stock_index_09 where code='${code}' and category='${category}'")).map{ row =>
      NaverStockIndex09(code, category, row.getString("y_index_201712"), row.getString("y_index_201812")
        , row.getString("y_index_201912"), row.getString("y_index_202012e")
        , row.getString("q_index_201906"), row.getString("q_index_201909"), row.getString("q_index_201912")
        , row.getString("q_index_202003"), row.getString("q_index_202006"), row.getString("q_index_202009e"))
    }.value
}
