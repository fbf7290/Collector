package com.asset.collector.api


import java.io.{CharArrayWriter, PrintWriter}

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.{DefaultExceptionSerializer, RawExceptionMessage}
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, MessageProtocol, TransportErrorCode, TransportException}
import play.api.{Environment, Mode}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

class ClientExceptionSerializer(environment: Environment) extends DefaultExceptionSerializer(environment) {
  override def serialize(exception: Throwable, accept: Seq[MessageProtocol]): RawExceptionMessage = {
    val (errorCode, messageBytes) = exception match {
      case ce: ClientException =>
        (ce.errorCode, ByteString.fromString(Json.stringify(ce.body)))
      case te: TransportException =>
        (te.errorCode, buildByteStringFromExceptionMessage(te.exceptionMessage))
      case e if environment.mode == Mode.Prod =>
        // By default, don't give out information about generic exceptions.
        (TransportErrorCode.InternalServerError, buildByteStringFromExceptionMessage(new ExceptionMessage("Exception", "")))
      case e =>
        // Ok to give out exception information in dev and test
        val writer = new CharArrayWriter
        e.printStackTrace(new PrintWriter(writer))
        val detail = writer.toString
        (TransportErrorCode.InternalServerError, buildByteStringFromExceptionMessage(new ExceptionMessage(s"${exception.getClass.getName}: ${exception.getMessage}", detail)))
    }

    RawExceptionMessage(errorCode, MessageProtocol(Some("application/json"), None, None), messageBytes)
  }

  private def buildByteStringFromExceptionMessage(message: ExceptionMessage) = {
    ByteString.fromString(Json.stringify(Json.obj(
      "name" -> message.name,
      "detail" -> message.detail
    )))
  }

  override def deserialize(message: RawExceptionMessage): Throwable = {
    val messageJson = try {
      Json.parse(message.message.iterator.asInputStream)
    } catch {
      case NonFatal(e) =>
        Json.obj()
    }

    //		System.out.println("-------------> " + message.errorCode.http + " : " + messageJson)

    message.errorCode.http match {
      case 400 => new ClientException(message.errorCode, messageJson)
      case 401 => new ClientException(message.errorCode, messageJson)
      case 403 => new ClientException(message.errorCode, messageJson)
      case 404 => new ClientException(message.errorCode, messageJson)
      case 409 => new ClientException(message.errorCode, messageJson)
      case 423 => new ClientException(message.errorCode, messageJson)
      case 429 => new ClientException(message.errorCode, messageJson)
      case _ =>
        val jsonParseResult = for {
          name <- (messageJson \ "name").validate[String]
          detail <- (messageJson \ "detail").validate[String]
        } yield new ExceptionMessage(name, detail)

        val exceptionMessage = jsonParseResult match {
          case JsSuccess(m, _) => m
          case JsError(_)      => new ExceptionMessage("UndeserializableException", message.message.utf8String)
        }

        fromCodeAndMessage(message.errorCode, exceptionMessage)
    }
  }
}


class ClientException(val code: Int, val name:String, val detail:String) extends RuntimeException(name) {

  def this(errorCode: TransportErrorCode, body: JsValue){
    this(errorCode.http, (body \ "name").validate[String].get,  (body \ "detail").validate[String].get)
  }
  val body = Json.obj("name"->name, "detail"->detail)
  val errorCode:TransportErrorCode = TransportErrorCode(code, 0, "")

  override def toString: String = Json.stringify(body)
}


class AuthorizationException(name:String, detail:String) extends ClientException(401, name, detail)