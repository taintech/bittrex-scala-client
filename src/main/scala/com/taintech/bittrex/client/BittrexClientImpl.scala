package com.taintech.bittrex.client

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import argonaut.DecodeJson
import com.taintech.bittrex.client.OrderBookType.OrderBookType
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

class BittrexClientImpl(val http: HttpExt, config: BittrexClientConfig)(
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
    request[List[Market]](s"$publicApiUrl/getmarkets")

  def getCurrencies: Future[List[CurrencyInfo]] =
    request[List[CurrencyInfo]](s"$publicApiUrl/getcurrencies")

  def getTicker(market: String): Future[Ticker] =
    request[Ticker](s"$publicApiUrl/getticker?market=$market")

  def getMarketSummaries: Future[List[MarketSummary]] =
    request[List[MarketSummary]](s"$publicApiUrl/getmarketsummaries")

  def getMarketSummary(market: String): Future[List[MarketSummary]] =
    request[List[MarketSummary]](
      s"$publicApiUrl/getmarketsummary?market=$market")

  def getOrderBook(market: String,
                   orderType: OrderBookType): Future[OrderBook] =
    request[OrderBook](
      s"$publicApiUrl/getorderbook?market=$market&type=$orderType")

  def getMarketHistory(market: String): Future[List[Trade]] =
    request[List[Trade]](s"$publicApiUrl/getmarkethistory?market=$market")

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

  private def httpGet(url: String): Future[HttpResponse] = {
    logger.debug(s"Performing get request to url: $url")
    val promisedResponse = Promise[HttpResponse]
    queue
      .offer(HttpRequest(uri = url) -> promisedResponse)
      .flatMap(_ => promisedResponse.future)
  }

  private def unmarshal[T: DecodeJson](
      response: Future[HttpResponse]): Future[T] =
    response.flatMap(r => Unmarshal(r.entity).to[T])

  private def request[T](url: String)(
      implicit decodeJson: DecodeJson[BittrexResponse[T]]): Future[T] = {
    unmarshal[BittrexResponse[T]](httpGet(url)) map {
      case BittrexResponse(true, _, Some(result)) => result
      case BittrexResponse(true, _, None) =>
        sys.error(s"Successful Bittrex response, but no result!")
      case BittrexResponse(false, message, _) =>
        sys.error(s"Failed with message $message")
      case _ => sys.error(s"Unexpected Bittrex response!")
    }
  }

}

case class BittrexClientConfig(host: String,
                               port: Int,
                               rootUrl: String,
                               publicApiUrl: String,
                               bufferSize: Int)
