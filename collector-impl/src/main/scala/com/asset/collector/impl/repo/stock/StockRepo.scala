package com.asset.collector.impl.repo.stock

import akka.Done
import com.asset.collector.api.Country.Country
import com.asset.collector.api.{Market, Stock}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.asset.collector.api.Market.Market

import scala.concurrent.{ExecutionContext, Future}


case class StockRepo(session: CassandraSession)(implicit val  ec: ExecutionContext) extends StockRepoTrait[Future]{
  override def createStockTable(country: Country): Future[Done] =
    session.executeCreateTable(s"create table if not exists ${country}_stock (ignored TEXT, code TEXT, name TEXT, market TEXT, PRIMARY KEY(ignored, code))")

  override def selectStocks(country: Country): Future[Seq[Stock]] =
    session.selectAll(s"select code, name, market from ${country}_stock").map{ rows =>
      rows.map(row => Stock(Market.toMarket(row.getString("market")).get, row.getString("name"), row.getString("code")))
    }

  override def insertStock(country: Country, code: String, name: String, market: Market): Future[Done] =
    session.executeWrite(s"INSERT INTO ${country}_stock (ignored, code, name, market) VALUES ('1', '${code}', '${name}', '${market}')")
}