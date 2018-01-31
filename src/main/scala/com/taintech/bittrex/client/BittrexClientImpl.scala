package com.taintech.bittrex.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.taintech.bittrex.client.OrderBookType.OrderBookType

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

class BittrexClientImpl(val http: HttpExt)(
    implicit system: ActorSystem,
    materializer: ActorMaterializer,
    executionContext: ExecutionContextExecutor)
    extends BittrexClient {

  private val pool = Http()
    .cachedHostConnectionPoolHttps[Promise[HttpResponse]](
      "bittrex.com",
      settings = ConnectionPoolSettings(system))

  private val queue = Source
    .queue[(HttpRequest, Promise[HttpResponse])](200, OverflowStrategy.dropNew)
    .via(pool)
    .toMat(Sink.foreach({
      case ((Success(resp), p)) => p.success(resp)
      case ((Failure(e), p))    => p.failure(e)
    }))(Keep.left)
    .run

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

  override def getMarkets: Future[List[Market]] = ???

  override def getCurrencies: Future[List[CurrencyInfo]] = ???

  override def getTicker(market: String): Future[Ticker] = ???

  override def getMarketSummaries: Future[List[MarketSummary]] = ???

  override def getMarketSummaries(market: String): Future[List[MarketSummary]] =
    ???

  override def getOrderBook(market: String,
                            orderType: OrderBookType): Future[OrderBook] = ???

  override def getMarketHistory(market: String): Future[List[Trade]] = ???

}
