package com.taintech.bittrex.client

final case class BittrexClientConfig(host: String,
                                     port: Int,
                                     apiPath: String,
                                     bufferSize: Option[Int],
                                     accountKey: Option[AccountKey])

final case class AccountKey(apiKey: String, apiSecret: String)

final case class DefaultAppConfig(bittrexClient: BittrexClientConfig)
