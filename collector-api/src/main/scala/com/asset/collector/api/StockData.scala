package com.asset.collector.api

import com.asset.collector.api.Market.Market
import play.api.libs.json.{Format, JsPath, JsResult, JsValue, Json, Reads, Writes}
import play.api.libs.functional.syntax._


sealed trait Category
case class Apartment(name1:String) extends Category
object Apartment{
  implicit val format: Format[Apartment] = Json.format
}
case class MultiRoom(name1:String) extends Category
object MultiRoom{
  implicit val format: Format[MultiRoom] = Json.format
}

case class Test(category:String, content:Category)
object Test{


  val reads: Reads[Test] =
    (JsPath \ "category").read[String].flatMap{ category =>
      category match {
        case "MultiRoom" => ((JsPath \ "category").read[String] and (JsPath \ "content").read[MultiRoom])(Test.apply _)
        case "Apartment" => ((JsPath \ "category").read[String] and (JsPath \ "content").read[Apartment])(Test.apply _)
      }
    }

  val writes = new Writes[Test] {
    def writes(o: Test) =
      o.category match {
        case "MultiRoom" => Json.obj("category"->o.category, "content"->o.content.asInstanceOf[MultiRoom])
        case "Apartment" => Json.obj("category"->o.category, "content"->o.content.asInstanceOf[Apartment])
      }
  }
  implicit val format: Format[Test] = Format(reads, writes)
}


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

case class Price(code:String, date:String, close:String, open:String, high:String, low:String, volume:String)
object Price {
  implicit val format :Format[Price]= Json.format
}

case class NaverStockIndex09(code:String, category:String, yIndex201712:String
                             , yIndex201812:String, yIndex201912:String, yIndex202012E:String
                             , qIndex201906:String, qIndex201909:String, qIndex201912:String
                             , qIndex202003:String, qIndex202006:String, qIndex202009E:String)
object NaverStockIndex09 {
  implicit val format :Format[NaverStockIndex09]= Json.format
  def apply(code:String, category:String, indexes:String*):NaverStockIndex09 =
    NaverStockIndex09(code, category, indexes(0), indexes(1), indexes(2), indexes(3), indexes(4), indexes(5), indexes(6), indexes(7), indexes(8), indexes(9))
}
