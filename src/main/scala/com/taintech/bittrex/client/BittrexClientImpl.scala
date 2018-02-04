package com.taintech.bittrex.client

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import argonaut.DecodeJson
import com.taintech.bittrex.client.OrderBookType.OrderBookType
import com.taintech.bittrex.client.OrderBookType._
import com.taintech.bittrex.client.codecs.ArgonautSupport
import com.typesafe.scalalogging.LazyLogging
import org.bouncycastle.util.encoders.Hex

import scala.collection.immutable
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

class BittrexClientImpl(http: HttpExt, config: BittrexClientConfig)(
    implicit system: ActorSystem,
    materializer: ActorMaterializer,
    executionContext: ExecutionContextExecutor)
    extends BittrexClient
    with ArgonautSupport
    with LazyLogging {

  import config._

  private val pool = http.cachedHostConnectionPoolHttps[Promise[HttpResponse]](
    host = host,
    port = port,
    settings = ConnectionPoolSettings(system))

  private val queue = Source
    .queue[(HttpRequest, Promise[HttpResponse])](bufferSize.getOrElse(100),
                                                 OverflowStrategy.dropNew)
    .via(pool)
    .toMat(Sink.foreach({
      case ((Success(resp), p)) => p.success(resp)
      case ((Failure(e), p))    => p.failure(e)
    }))(Keep.left)
    .run

  def getMarkets: Future[List[Market]] =
    getPublic[List[Market]]("getmarkets")

  def getCurrencies: Future[List[CurrencyInfo]] =
    getPublic[List[CurrencyInfo]](s"getcurrencies")

  def getTicker(market: String): Future[Ticker] =
    getPublic[Ticker]("getticker", Map("market" -> market))

  def getMarketSummaries: Future[List[MarketSummary]] =
    getPublic[List[MarketSummary]]("getmarketsummaries")

  def getMarketSummary(market: String): Future[List[MarketSummary]] =
    getPublic[List[MarketSummary]]("getmarketsummary", Map("market" -> market))

  def getOrderBook(market: String,
                   orderType: OrderBookType): Future[OrderBook] = {
    orderType match {
      case Both =>
        getPublic[OrderBook](
          "getorderbook",
          Map("market" -> market, "type" -> orderType.toString))
      case Sell =>
        getPublic[List[Order]](
          "getorderbook",
          Map("market" -> market, "type" -> orderType.toString))
          .map(OrderBook(List.empty, _))
      case Buy =>
        getPublic[List[Order]](
          "getorderbook",
          Map("market" -> market, "type" -> orderType.toString))
          .map(OrderBook(_, List.empty))
    }
  }

  def getMarketHistory(market: String): Future[List[Trade]] =
    getPublic[List[Trade]]("getmarkethistory", Map("market" -> market))

  override def buyLimit(market: String,
                        quantity: BigDecimal,
                        rate: BigDecimal): Future[OrderUuid] =
    queryMarket[OrderUuid]("buylimit",
                           Map(
                             "market" -> market,
                             "quantity" -> quantity.toString(),
                             "rate" -> rate.toString()
                           ))

  override def sellLimit(market: String,
                         quantity: BigDecimal,
                         rate: BigDecimal): Future[OrderUuid] =
    queryMarket[OrderUuid]("selllimit",
                           Map(
                             "market" -> market,
                             "quantity" -> quantity.toString(),
                             "rate" -> rate.toString()
                           ))

  override def cancel(orderUuid: OrderUuid): Future[OrderUuid] =
    queryMarket[OrderUuid]("cancel",
                           Map(
                             "uuid" -> orderUuid.value
                           ))

  override def openOrders(market: String): Future[List[OpenOrder]] =
    queryMarket[List[OpenOrder]]("getopenorders",
                                 Map(
                                   "market" -> market
                                 ))

  override def getBalances: Future[List[Balance]] =
    queryAccount[List[Balance]]("getbalances")

  override def getBalance(currency: String): Future[Balance] =
    queryAccount[Balance]("getbalance",
                          Map(
                            "currency" -> currency
                          ))

  override def getAddress(currency: String): Future[CryptoAddress] =
    queryAccount[CryptoAddress]("getdepositaddress",
                                Map(
                                  "currency" -> currency
                                ))

  override def accountWithdraw(currency: String,
                               quantity: BigDecimal,
                               address: String,
                               paymentId: Option[String]): Future[OrderUuid] =
    queryAccount[OrderUuid]("withdraw",
                            Map(
                              "currency" -> currency,
                              "quantity" -> quantity.toString(),
                              "address" -> address
                            ) ++ paymentId.map(("paymentid", _)).toMap)

  override def getOrder(orderUuid: OrderUuid): Future[ClosedOrder] =
    queryAccount[ClosedOrder]("getorder",
                              Map(
                                "uuid" -> orderUuid.value
                              ))

  override def getOrderHistory(
      market: Option[String]): Future[List[OrderHistory]] =
    queryAccount[List[OrderHistory]]("getorderhistory",
                                     market.map(("market", _)).toMap)

  override def getWithdrawalHistory(
      currency: Option[String]): Future[List[Withdrawal]] =
    queryAccount[List[Withdrawal]]("getwithdrawalhistory",
                                   currency.map(("currency", _)).toMap)

  override def getDepositHistory(
      currency: Option[String]): Future[List[Deposit]] =
    queryAccount[List[Deposit]]("getdeposithistory",
                                currency.map(("currency", _)).toMap)

  private def getPublic[T](method: String,
                           params: Map[String, String] = Map.empty)(
      implicit decodeJson: DecodeJson[BittrexResponse[T]]) =
    get[T](s"$apiPath/public/$method", params, signed = false)

  private def queryMarket[T](method: String, params: Map[String, String])(
      implicit decodeJson: DecodeJson[BittrexResponse[T]]) =
    get[T](s"$apiPath/market/$method", params, signed = true)

  private def queryAccount[T](method: String,
                              params: Map[String, String] = Map.empty)(
      implicit decodeJson: DecodeJson[BittrexResponse[T]]) =
    get[T](s"$apiPath/account/$method", params, signed = true)

  private def get[T](url: String, params: Map[String, String], signed: Boolean)(
      implicit decodeJson: DecodeJson[BittrexResponse[T]]): Future[T] = {
    val httpResponse =
      if (signed) signedRequest(url, params)
      else performHttpGet(buildUrl(url, params))
    val response = unmarshal[BittrexResponse[T]](httpResponse)
    handleResponse(response)
  }

  private def performHttpGet(url: String,
                             headers: immutable.Seq[HttpHeader] = Nil) = {
    val promisedResponse = Promise[HttpResponse]
    logger.debug(
      s"Performing get request using port $port to url: $url with headers $headers")
    queue
      .offer(HttpRequest(uri = url, headers = headers) -> promisedResponse)
      .flatMap(_ => promisedResponse.future)
  }

  private def unmarshal[T: DecodeJson](
      response: Future[HttpResponse]): Future[T] =
    response.flatMap(r => Unmarshal(r.entity).to[T])

  private def handleResponse[T](
      response: Future[BittrexResponse[T]]): Future[T] = response map {
    case BittrexResponse(true, _, Some(result)) => result
    case BittrexResponse(true, _, None) =>
      sys.error(s"Successful Bittrex response, but no result!")
    case BittrexResponse(false, message, _) =>
      sys.error(s"Failed with message $message")
    case _ => sys.error(s"Unexpected Bittrex response!")
  }

  private def buildUrl(baseUrl: String, params: Map[String, String]): String = {
    val url =
      if (params.isEmpty) baseUrl
      else
        baseUrl + params.map { case (k, v) => s"$k=$v" }.mkString("?", "&", "")
    s"https://$host$url"
  }

  private def apiSign(url: String, apiSecret: String) = {
    val keyBytes = apiSecret.getBytes("UTF-8")
    val HMAC_SHA256 = "HmacSHA512"
    val sha512_HMAC = Mac.getInstance(HMAC_SHA256)
    val keySpec = new SecretKeySpec(keyBytes, HMAC_SHA256)
    sha512_HMAC.init(keySpec)
    val dataBytes = sha512_HMAC.doFinal(url.getBytes("UTF-8"))
    Hex.toHexString(dataBytes)
  }

  private def signedRequest(baseUrl: String,
                            params: Map[String, String]): Future[HttpResponse] =
    accountKey match {
      case None => Future.failed(sys.error("No account key in configurations!"))
      case Some(AccountKey(apiKey, apiSecret)) =>
        val apiKeyParams =
          Map("apiKey" -> apiKey,
              "nonce" -> System.currentTimeMillis().toString)
        val url = buildUrl(baseUrl, apiKeyParams ++ params)
        val apiSignHeader = List(RawHeader("apisign", apiSign(url, apiSecret)))
        performHttpGet(url, apiSignHeader)
    }

}
