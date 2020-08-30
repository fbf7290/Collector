package com.asset.collector.api

import play.api.libs.json.{Format, Json}

case class DumbStock(ticker:String, name:String, is_etf:Option[String], exchange:String)
object DumbStock {
  implicit val format :Format[DumbStock]= Json.format
}


case class NaverEtf(itemcode:String, etfTabCode:Option[Int], itemname:String, nowVal:Option[Int]
                    , risefall:Option[String], changeVal:Option[Int], changeRate:Option[Float], nav:Option[Float]
                    , threeMonthEarnRate:Option[Float], quant:Option[Int], amonut:Option[Int], marketSum:Option[Int])
object NaverEtf {
  implicit val format :Format[NaverEtf]= Json.format
}

case class NaverEtfList(etfItemList:Seq[NaverEtf])
object NaverEtfList {
  implicit val format :Format[NaverEtfList]= Json.format
}

case class NaverEtfListResponse(resultCode:String, result:NaverEtfList)
object NaverEtfListResponse {
  implicit val format :Format[NaverEtfListResponse]= Json.format
}
