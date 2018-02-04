package com.taintech.bittrex.client

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.taintech.bittrex.client.OrderBookType.OrderBookType
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

trait BittrexClient extends PublicApi with MarketApi with AccountApi

sealed trait RestApi

trait PublicApi extends RestApi {

  def getMarkets: Future[List[Market]]

  def getCurrencies: Future[List[CurrencyInfo]]

  def getTicker(market: String): Future[Ticker]

  def getMarketSummaries: Future[List[MarketSummary]]

  def getMarketSummary(market: String): Future[List[MarketSummary]]

  def getOrderBook(market: String, orderType: OrderBookType): Future[OrderBook]

  def getMarketHistory(market: String): Future[List[Trade]]

}

trait MarketApi extends RestApi {

  def buyLimit(market: String,
               quantity: BigDecimal,
               rate: BigDecimal): Future[OrderUuid]

  def sellLimit(market: String,
                quantity: BigDecimal,
                rate: BigDecimal): Future[OrderUuid]

  def cancel(orderUuid: OrderUuid): Future[Done]

  def openOrders(market: String): Future[List[OpenOrder]]

}

trait AccountApi extends RestApi {

  def getBalances: Future[List[Balance]]

  def getBalance(currency: String): Future[Balance]

  def getDepositAddress(currency: String): Future[CryptoAddress]

  def accountWithdraw(currency: String,
                      quantity: BigDecimal,
                      address: String,
                      paymentId: Option[String]): Future[OrderUuid]

  def getOrder(orderUuid: OrderUuid): Future[ClosedOrder]

  def getOrderHistory(market: Option[String]): Future[List[OrderHistory]]

  def getWithdrawalHistory(
      currency: Option[String]): Future[List[WithdrawalHistory]]

  def getDepositHistory(currency: Option[String]): Future[List[DepositHistory]]

}

object BittrexClient {

  def apply(http: HttpExt, config: BittrexClientConfig)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor): BittrexClient =
    new BittrexClientImpl(http, config)

  def apply(bittrexClientConfig: BittrexClientConfig)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor): BittrexClient =
    apply(Http(), bittrexClientConfig)

  def apply()(implicit system: ActorSystem,
              materializer: ActorMaterializer,
              executionContext: ExecutionContextExecutor): BittrexClient =
    apply(
      pureconfig
        .loadConfig[DefaultAppConfig](ConfigFactory.load())
        .fold(err =>
                sys.error(s"Failed to load default app configurations $err"),
              _.bittrexClient))

}
