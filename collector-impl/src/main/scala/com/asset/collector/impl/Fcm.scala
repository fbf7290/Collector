package com.asset.collector.impl

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import com.asset.collector.api.CollectorSettings

object Fcm {

  case class FcmMessage(title:String, body:String ,data:JsValue)


  def sendFcmMsg(registrationIds:List[String], msg:FcmMessage, priority:String = "high")(implicit wsClient:WSClient) =
    wsClient.url(CollectorSettings.fcmBaseUrl)
      .addHttpHeaders("Authorization"->s"key=${CollectorSettings.fcmProjectKey}")
      .post(
        body = Json.obj("registration_ids"->registrationIds, "notification"->Json.obj("title"->msg.title, "body"->msg.body, "data"->msg.data, "priority"->priority))
      )
}
