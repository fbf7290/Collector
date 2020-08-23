package com.asset.collector.impl.repo.price

import akka.Done
import com.asset.collector.api.Country.Country
import com.asset.collector.api.Price
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.concurrent.{ExecutionContext, Future}

case class PriceRepo(session: CassandraSession)(implicit val  ec: ExecutionContext) extends PriceRepoTrait[Future] {
  override def createPriceTable(country: Country): Future[Done] =
    session.executeCreateTable(s"create table if not exists ${country}_price (code TEXT, timestamp BIGINT, close INT, open INT, low INT, high INT, PRIMARY KEY(code, timestamp)) WITH CLUSTERING ORDER BY (timestamp DESC)")

  override def selectLatestTimestamp(country: Country, code: String): Future[Long] =
    session.selectOne(s"select timestamp from ${country}_price where code='${code}").map(_.fold(0.toLong)(_.getLong("timestamp")))

  override def insertPrice(country:Country, price: Price): Future[Done] =
    session.executeWrite(s"INSERT INTO ${country}_price (code, timestamp, close, open, low, high VALUES ('${price.code}', ${price.timestamp}, ${price.close}, ${price.open}, ${price.low}, ${price.high}")
}
