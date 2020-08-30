package com.asset.collector.api

import com.asset.collector.api.Market.Market
import play.api.libs.json.{Format, Json}

object Country extends Enumeration {
  type Country = Value

  val KOREA = Value("korea")
  val USA = Value("usa")

  implicit val format1: Format[Country] = Json.formatEnum(Country)

  def toCountry(value:String):Option[Country] = if(value=="korea") Some(KOREA) else if(value=="usa") Some(USA) else None
}


object Market extends Enumeration {
  type Market = Value

  val KOSPI = Value("kospi")
  val KOSDAQ = Value("kosdaq")
  val NASDAQ = Value("nasdaq")
  val DOW = Value("dow")
  val SP500 = Value("sp500")
  val NYSE = Value("nyse")
  val AMEX = Value("amex")


  implicit val format1: Format[Market] = Json.formatEnum(Market)

  def toMarket(value:String):Option[Market] = value match {
    case "kospi" => Some(KOSPI)
    case "kosdaq" => Some(KOSDAQ)
    case "nasdaq" => Some(NASDAQ)
    case "dow" => Some(DOW)
    case "sp500" => Some(SP500)
    case "nyse" => Some(NYSE)
    case "amex" => Some(AMEX)
    case _ => None
  }
}


case class Stock(market:Market, name:String, code:String){
  override def canEqual(a: Any) = a.isInstanceOf[Stock]

  override def equals(that: Any): Boolean =
    that match {
      case that: Stock =>
        that.canEqual(this) && this.hashCode == that.hashCode
      case _ => false
    }

  override def hashCode:Int = {
    code.hashCode
  }

}
object Stock {
  implicit val format :Format[Stock]= Json.format
}

case class Price(code:String, date:Int, close:Int, open:Int, high:Int, low:Int, volume:Long)
object Price {
  implicit val format :Format[Price]= Json.format
}
