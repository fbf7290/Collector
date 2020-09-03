package com.asset.collector.api

object CollectorSettings {
  val fcmProjectKey = sys.env.get("FCM_PROJECT_KEY") match {
    case Some(fcm_project_key) => fcm_project_key
    case None => ""
  }

  val fcmBaseUrl = sys.env.get("FCM_BASE_URL") match {
    case Some(fcm_base_url) => fcm_base_url
    case None => "https://fcm.googleapis.com/fcm/send"
  }

  val adminFcmRegistrationId = sys.env.get("ADMIN_FCM_ID") match {
    case Some(admin_fcm_id) => admin_fcm_id
    case None => ""
  }
}
