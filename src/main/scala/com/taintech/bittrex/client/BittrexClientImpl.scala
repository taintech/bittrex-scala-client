package com.taintech.bittrex.client

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
    .queue[(HttpRequest, Promise[HttpResponse])](bufferSize,
                                                 OverflowStrategy.dropNew)
    .via(pool)
    .toMat(Sink.foreach({
      case ((Success(resp), p)) => p.success(resp)
      case ((Failure(e), p))    => p.failure(e)
    }))(Keep.left)
    .run

  def getMarkets: Future[List[Market]] =
    get[List[Market]](s"$publicApiUrl/getmarkets")

  def getCurrencies: Future[List[CurrencyInfo]] =
    get[List[CurrencyInfo]](s"$publicApiUrl/getcurrencies")

  def getTicker(market: String): Future[Ticker] =
    get[Ticker](s"$publicApiUrl/getticker", Map("market" -> market))

  def getMarketSummaries: Future[List[MarketSummary]] =
    get[List[MarketSummary]](s"$publicApiUrl/getmarketsummaries")

  def getMarketSummary(market: String): Future[List[MarketSummary]] =
    get[List[MarketSummary]](s"$publicApiUrl/getmarketsummary",
                             Map("market" -> market))

  def getOrderBook(market: String,
                   orderType: OrderBookType): Future[OrderBook] = {
    orderType match {
      case Both =>
        get[OrderBook](s"$publicApiUrl/getorderbook",
                       Map("market" -> market, "type" -> orderType.toString))
      case Sell =>
        get[List[Order]](s"$publicApiUrl/getorderbook",
                         Map("market" -> market, "type" -> orderType.toString))
          .map(OrderBook(List.empty, _))
      case Buy =>
        get[List[Order]](s"$publicApiUrl/getorderbook",
                         Map("market" -> market, "type" -> orderType.toString))
          .map(OrderBook(_, List.empty))
    }
  }

  def getMarketHistory(market: String): Future[List[Trade]] =
    get[List[Trade]](s"$publicApiUrl/getmarkethistory", Map("market" -> market))

  override def getBalances: Future[List[Balance]] = ???

  override def getBalance(currency: String): Future[Balance] = ???

  override def getAddress(currency: String): Future[CryptoAddress] = ???

  override def accountWithdraw(currency: String,
                               quantity: BigDecimal,
                               address: String,
                               paymentId: Option[String]): Future[OrderUuid] =
    ???

  override def getOrder(orderUuid: OrderUuid): Future[ClosedOrder] = ???

  override def getOrderHistory(
      market: Option[String]): Future[List[OrderHistory]] = ???

  override def getWithdrawalHistory(
      currency: Option[String]): Future[List[Withdrawal]] = ???

  override def getDepositHistory(
      currency: Option[String]): Future[List[Deposit]] = ???

  override def buyLimit(market: String,
                        quantity: BigDecimal,
                        rate: BigDecimal): Future[OrderUuid] = ???

  override def sellLimit(market: String,
                         quantity: BigDecimal,
                         rate: BigDecimal): Future[OrderUuid] = ???

  override def cancel(orderUuid: OrderUuid): Future[OrderUuid] = ???

  override def openOrders(market: String): Future[List[OpenOrder]] = ???

  def apiKey: String = ???

  def apiSign(requestUrl: String): String = ???

  private def buildUrl(baseUrl: String, params: Map[String, String]): String = {
    val url =
      if (params.isEmpty) baseUrl
      else
        baseUrl + params.map { case (k, v) => s"$k=$v" }.mkString("?", "&", "")
    s"https://$host:$port$url"
  }

  private def signedRequest(
      baseUrl: String,
      params: Map[String, String]): Future[HttpResponse] = {
    val apiKeyParams =
      Map("apiKey" -> apiKey, "nonce" -> System.currentTimeMillis().toString)
    val url = buildUrl(baseUrl, apiKeyParams ++ params)
    val apiSignHeader = List(RawHeader("apisign", apiSign(url)))
    performHttpGet(url, apiSignHeader)
  }

  private def performHttpGet(url: String,
                             headers: immutable.Seq[HttpHeader] = Nil) = {
    val promisedResponse = Promise[HttpResponse]
    logger.debug(s"Performing get request to url: $url with headers $headers")
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

  private def get[T](url: String,
                     params: Map[String, String] = Map.empty,
                     signed: Boolean = false)(
      implicit decodeJson: DecodeJson[BittrexResponse[T]]): Future[T] = {
    val httpResponse =
      if (signed) signedRequest(url, params)
      else performHttpGet(buildUrl(url, params))
    val response = unmarshal[BittrexResponse[T]](httpResponse)
    handleResponse(response)
  }

}

case class BittrexClientConfig(host: String,
                               port: Int,
                               rootUrl: String,
                               publicApiUrl: String,
                               bufferSize: Int)
