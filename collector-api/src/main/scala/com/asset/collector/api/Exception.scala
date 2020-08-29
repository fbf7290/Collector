package com.asset.collector.api

object Exception {
  object ExternalResourceException extends ClientException(404, "ExternalResourceException", "Check your external resource")
}