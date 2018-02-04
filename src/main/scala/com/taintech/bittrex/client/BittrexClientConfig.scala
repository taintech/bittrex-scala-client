package com.taintech.bittrex.client

case class BittrexClientConfig(host: String,
                               port: Int,
                               apiPath: String,
                               bufferSize: Option[Int],
                               accountKey: Option[AccountKey])

case class AccountKey(apiKey: String, apiSecret: String)

case class DefaultAppConfig(bittrexClient: BittrexClientConfig)