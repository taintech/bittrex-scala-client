package com.taintech.bittrex.client

import akka.Done
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ PathMatchers, Route }
import akka.stream.scaladsl.StreamConverters
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.{ Matchers, WordSpec }

import scala.language.postfixOps

class BittrexClientSpec extends WordSpec with Matchers with ScalaFutures with BittrexHTTPSTestServer {

  import BittrexClientSpec._

  val testClient = BittrexClient(testConf)

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(5, Millis)))

  "BittrexClient" should {
    "return markets" in {
      whenReady(testClient.getMarkets) { result =>
        result should not be empty
        result should have size 263
        result should contain(market)
      }
    }
    "return currencies" in {
      whenReady(testClient.getCurrencies) { result =>
        result should not be empty
        result should have size 289
        result should contain(currencyInfo)
      }
    }
    "return ticker for Litecoin" in {
      whenReady(testClient.getTicker("BTC-LTC")) { result =>
        result should be(litecoinTicker)
      }
    }
    "return ticker for Ethereum" in {
      whenReady(testClient.getTicker("BTC-ETH")) { result =>
        result should be(ethereumTicker)
      }
    }
    "return failed exception for bla ticker" in {
      whenReady(testClient.getTicker("bla-bla").failed) { exception =>
        exception.getMessage should include("INVALID_MARKET")
      }
    }
    "return market summaries" in {
      whenReady(testClient.getMarketSummaries) { result =>
        result should not be empty
        result should have size 263
        result should contain(marketSummary)
      }
    }
    "return market summary for Litecoin" in {
      whenReady(testClient.getMarketSummary("BTC-LTC")) { result =>
        result should not be empty
        result should have size 1
        result should be(List(litecoinMarketSummary))
      }
    }
    "return market summary for Ethereum" in {
      whenReady(testClient.getMarketSummary("BTC-ETH")) { result =>
        result should not be empty
        result should have size 1
        result should be(List(ethereumMarketSummary))
      }
    }
    "return failed exception for bla market summary" in {
      whenReady(testClient.getMarketSummary("bla-bla").failed) { exception =>
        exception.getMessage should include("INVALID_MARKET")
      }
    }
    "return both types of order book for Litecoin" in {
      whenReady(testClient.getOrderBook("BTC-LTC", OrderBookType.Both)) { result =>
        result.buyOrders should not be empty
        result.sellOrders should not be empty
        result.buyOrders should have size 100
        result.sellOrders should have size 100
        result.buyOrders should contain(litecoinBuyOrder)
        result.sellOrders should contain(litecoinSellOrder)
      }
    }
    "return sell order book for Litecoin" in {
      whenReady(testClient.getOrderBook("BTC-LTC", OrderBookType.Sell)) { result =>
        result.buyOrders should be(List.empty)
        result.sellOrders should not be empty
        result.sellOrders should have size 500
        result.sellOrders should contain(litecoinSellOrder)
      }
    }
    "return buy order book for Litecoin" in {
      whenReady(testClient.getOrderBook("BTC-LTC", OrderBookType.Buy)) { result =>
        result.buyOrders should not be empty
        result.sellOrders should be(List.empty)
        result.buyOrders should have size 500
        result.buyOrders should contain(litecoinBuyOrder)
      }
    }
    "return both types of order book for Ethereum" in {
      whenReady(testClient.getOrderBook("BTC-ETH", OrderBookType.Both)) { result =>
        result.buyOrders should not be empty
        result.sellOrders should not be empty
        result.buyOrders should have size 100
        result.sellOrders should have size 100
        result.buyOrders should contain(ethereumBuyOrder)
        result.sellOrders should contain(ethereumSellOrder)
      }
    }
    "return failed exception for bla order book" in {
      whenReady(testClient.getOrderBook("bla-bla", OrderBookType.Both).failed) { exception =>
        exception.getMessage should include("INVALID_MARKET")
      }
    }
    "return market history for Litecoin" in {
      whenReady(testClient.getMarketHistory("BTC-LTC")) { result =>
        result should not be empty
        result should have size 100
        result should contain(litecoinTrade)
      }
    }
    "return market history for Ethereum" in {
      whenReady(testClient.getMarketHistory("BTC-ETH")) { result =>
        result should not be empty
        result should have size 100
        result should contain(ethereumTrade)
      }
    }
    "return failed exception for bla market history" in {
      whenReady(testClient.getMarketHistory("bla-bla").failed) { exception =>
        exception.getMessage should include("INVALID_MARKET")
      }
    }
    "place buy limit order for Litecoin" in {
      whenReady(testClient.buyLimit("BTC-LTC", 69.69, 96.96)) { result =>
        result should be(testOrderUuid)
      }
    }
    "place sell limit order for Litecoin" in {
      whenReady(testClient.sellLimit("BTC-LTC", 96.96, 69.69)) { result =>
        result should be(testOrderUuid)
      }
    }
    "cancel an order" in {
      whenReady(testClient.cancel(testOrderUuid)) { result =>
        result should be(Done)
      }
    }
    "get open orders for Litecoin" in {
      whenReady(testClient.openOrders("BTC-LTC")) { result =>
        result should not be empty
        result should have size 2
        result should contain(testOpenOrder)
      }
    }
    "get account balances" in {
      whenReady(testClient.getBalances) { result =>
        result should not be empty
        result should have size 2
        result should contain(testBitcoinBalance1)
      }
    }
    "get account balance for Bitcoin" in {
      whenReady(testClient.getBalance("BTC")) { result =>
        result should be(testBitcoinBalance2)
      }
    }
    "get account deposit address for Bitcoin" in {
      whenReady(testClient.getDepositAddress("BTC")) { result =>
        result should be(testCryptoAddress)
      }
    }
    "withdraw Bitcoins from account" in {
      whenReady(testClient.accountWithdraw("BTC", 69.69, "Vy5SKeKGXUHKS2WVpJ76HYuKAu3URastUo", Some("test-payment-id"))) {
        result =>
          result should be(testOrderUuid)
      }
    }
    "get account open order" in {
      whenReady(testClient.getOrder(testOrderUuid)) { result =>
        result should be(testClosedOrder)
      }
    }
    "get account order history" in {
      whenReady(testClient.getOrderHistory(Some("BTC-LTC"))) { result =>
        result should not be empty
        result should have size 2
        result should contain(testOrderHistory)
      }
    }
    "get account withdrawal history" in {
      whenReady(testClient.getWithdrawalHistory(Some("BTC"))) { result =>
        result should not be empty
        result should have size 2
        result should contain(testWithdrawalHistory)
      }
    }
    "get account deposit history" in {
      whenReady(testClient.getDepositHistory(Some("BTC"))) { result =>
        result should not be empty
        result should have size 2
        result should contain(testDepositHistory)
      }
    }

  }

  override val hostname: String = host
  override val port: Int        = testServerPort

  private def resourceRoute(url: String, resource: String, expectedParams: Map[String, String]) =
    (get & path(PathMatchers.separateOnSlashes(url))) {
      parameterMap { params =>
        if (params == expectedParams) {
          complete(
            HttpResponse(
              status = StatusCodes.OK,
              headers = Nil,
              entity = HttpEntity.Chunked(
                contentType = ContentTypes.`application/json`,
                chunks = StreamConverters
                  .fromInputStream(() => getClass.getClassLoader.getResourceAsStream(resource))
                  .map(ChunkStreamPart.apply)
              )
            )
          )
        } else reject
      }
    } ~ reject

  private def resourceRouteWithApiSign(url: String, resource: String, expectedParams: Map[String, String]) =
    headerValueByName("apisign") { _ =>
      parameterMap { params =>
        resourceRoute(url, resource, expectedParams ++ params.filterKeys(_ == "nonce") ++ Map("apiKey" -> apiKey))
      }
    } ~ reject

  private def publicApiRoute(url: String,
                             resource: String,
                             expectedParams: Map[String, String] = Map.empty[String, String]) =
    resourceRoute(s"$apiPath/public/$url", s"public-api/$resource", expectedParams)

  private def marketApiRoute(url: String,
                             resource: String,
                             expectedParams: Map[String, String] = Map.empty[String, String]) =
    resourceRouteWithApiSign(s"$apiPath/market/$url", s"market-api/$resource", expectedParams)

  private def accountApiRoute(url: String,
                              resource: String,
                              expectedParams: Map[String, String] = Map.empty[String, String]) =
    resourceRouteWithApiSign(s"$apiPath/account/$url", s"account-api/$resource", expectedParams)

  val publicApiRoutes: Route = publicApiRoute("getmarkets", "getmarkets.json") ~
    publicApiRoute("getcurrencies", "getcurrencies.json") ~
    publicApiRoute("getmarketsummaries", "getmarketsummaries.json") ~
    publicApiRoute("getticker", "getticker-btc-ltc.json", Map("market"               -> "BTC-LTC")) ~
    publicApiRoute("getticker", "getticker-btc-eth.json", Map("market"               -> "BTC-ETH")) ~
    publicApiRoute("getticker", "getticker-bla-bla.json", Map("market"               -> "bla-bla")) ~
    publicApiRoute("getmarketsummary", "getmarketsummary-btc-ltc.json", Map("market" -> "BTC-LTC")) ~
    publicApiRoute("getmarketsummary", "getmarketsummary-btc-eth.json", Map("market" -> "BTC-ETH")) ~
    publicApiRoute("getmarketsummary", "getmarketsummary-bla-bla.json", Map("market" -> "bla-bla")) ~
    publicApiRoute("getorderbook", "getorderbook-btc-ltc-both.json", Map("market"    -> "BTC-LTC", "type" -> "both")) ~
    publicApiRoute("getorderbook", "getorderbook-btc-ltc-sell.json", Map("market"    -> "BTC-LTC", "type" -> "sell")) ~
    publicApiRoute("getorderbook", "getorderbook-btc-ltc-buy.json", Map("market"     -> "BTC-LTC", "type" -> "buy")) ~
    publicApiRoute("getorderbook", "getorderbook-btc-eth-both.json", Map("market"    -> "BTC-ETH", "type" -> "both")) ~
    publicApiRoute("getorderbook", "getorderbook-bla-bla-both.json", Map("market"    -> "bla-bla", "type" -> "both")) ~
    publicApiRoute("getmarkethistory", "getmarkethistory-btc-ltc.json", Map("market" -> "BTC-LTC")) ~
    publicApiRoute("getmarkethistory", "getmarkethistory-btc-eth.json", Map("market" -> "BTC-ETH")) ~
    publicApiRoute("getmarkethistory", "getmarkethistory-bla-bla.json", Map("market" -> "bla-bla"))

  protected val marketApiRoutes: Route = marketApiRoute(
    "buylimit",
    "buylimit.json",
    Map(
      "market"   -> "BTC-LTC",
      "quantity" -> "69.69",
      "rate"     -> "96.96"
    )
  ) ~ marketApiRoute(
    "selllimit",
    "selllimit.json",
    Map(
      "market"   -> "BTC-LTC",
      "quantity" -> "96.96",
      "rate"     -> "69.69"
    )
  ) ~ marketApiRoute(
    "cancel",
    "cancel.json",
    Map(
      "uuid" -> "e606d53c-8d70-11e3-94b5-425861b86ab6",
    )
  ) ~ marketApiRoute(
    "getopenorders",
    "getopenorders.json",
    Map(
      "market" -> "BTC-LTC",
    )
  )

  protected val accountApiRoutes: Route = accountApiRoute("getbalances", "getbalances.json") ~
    accountApiRoute(
      "getbalance",
      "getbalance.json",
      Map(
        "currency" -> "BTC"
      )
    ) ~ accountApiRoute(
    "getbalance",
    "getbalance.json",
    Map(
      "currency" -> "BTC"
    )
  ) ~ accountApiRoute(
    "getdepositaddress",
    "getdepositaddress.json",
    Map(
      "currency" -> "BTC"
    )
  ) ~ accountApiRoute(
    "withdraw",
    "withdraw.json",
    Map(
      "currency"  -> "BTC",
      "quantity"  -> "69.69",
      "address"   -> "Vy5SKeKGXUHKS2WVpJ76HYuKAu3URastUo",
      "paymentid" -> "test-payment-id"
    )
  ) ~ accountApiRoute(
    "getorder",
    "getorder.json",
    Map(
      "uuid" -> "e606d53c-8d70-11e3-94b5-425861b86ab6"
    )
  ) ~ accountApiRoute(
    "getorderhistory",
    "getorderhistory.json",
    Map(
      "market" -> "BTC-LTC"
    )
  ) ~ accountApiRoute(
    "getwithdrawalhistory",
    "getwithdrawalhistory.json",
    Map(
      "currency" -> "BTC"
    )
  ) ~ accountApiRoute(
    "getdeposithistory",
    "getdeposithistory.json",
    Map(
      "currency" -> "BTC"
    )
  )

}

object BittrexClientSpec {

  val host                = "localhost"
  val testServerPort: Int = 9069
  val bufferSize          = 10
  val apiPath             = "test"
  val apiKey              = "just4test-key"
  val apiSecret           = "just4test-secret"
  val accountKey          = AccountKey(apiKey, apiSecret)
  val testConf =
    BittrexClientConfig(host, testServerPort, "/" + apiPath, Some(bufferSize), Some(accountKey))

  val market = Market(
    marketName = "BTC-LTC",
    minTradeSize = 0.01469482,
    marketCurrency = "LTC",
    baseCurrencyLong = "Bitcoin",
    logoUrl = Some("https://bittrexblobstorage.blob.core.windows.net/public/6defbc41-582d-47a6-bb2e-d0fa88663524.png"),
    marketCurrencyLong = "Litecoin",
    baseCurrency = "BTC",
    notice = None,
    isActive = true,
    isSponsored = None,
    created = "2014-02-13T00:00:00"
  )

  val currencyInfo = CurrencyInfo(
    coinType = "BITCOIN",
    currencyLong = "Bitcoin",
    minConfirmation = 2,
    baseAddress = Some("1N52wHoVR79PMDishab2XmRHsbekCdGquK"),
    notice = None,
    isActive = true,
    currency = "BTC",
    txFee = 0.001
  )

  val marketSummary = MarketSummary(
    marketName = "BTC-1ST",
    openBuyOrders = 415,
    timeStamp = "2018-01-31T17:53:36.27",
    prevDay = 0.00007106,
    baseVolume = 293.51326005,
    last = 0.000071,
    low = 0.00006464,
    displayMarketName = None,
    openSellOrders = 5977,
    ask = 0.00007138,
    bid = 0.00007101,
    volume = 4275308.48269257,
    high = 0.00007219,
    created = "2017-06-06T01:22:35.727"
  )

  val litecoinTicker = Ticker(
    bid = 0.0160016,
    ask = 0.01603805,
    last = 0.01603805
  )

  val ethereumTicker = Ticker(
    bid = 0.11036129,
    ask = 0.1106005,
    last = 0.11036127
  )

  val litecoinMarketSummary = MarketSummary(
    marketName = "BTC-LTC",
    openBuyOrders = 1951,
    timeStamp = "2018-01-31T17:55:36.007",
    prevDay = 0.01642986,
    baseVolume = 783.61820524,
    last = 0.01603806,
    low = 0.0160001,
    displayMarketName = None,
    openSellOrders = 7580,
    ask = 0.01607539,
    bid = 0.01605905,
    volume = 48121.23913564,
    high = 0.01656499,
    created = "2014-02-13T00:00:00"
  )

  val ethereumMarketSummary = MarketSummary(
    marketName = "BTC-ETH",
    openBuyOrders = 5469,
    timeStamp = "2018-01-31T17:55:59.49",
    prevDay = 0.107805,
    baseVolume = 4831.48078226,
    last = 0.1106,
    low = 0.1051,
    displayMarketName = None,
    openSellOrders = 3303,
    ask = 0.11060049,
    bid = 0.1106,
    volume = 44989.1576027,
    high = 0.11088888,
    created = "2015-08-14T09:02:24.817"
  )

  val litecoinBuyOrder = Order(0.64425443, 0.01599691)

  val litecoinSellOrder = Order(0.0672977, 0.0162325)

  val ethereumBuyOrder = Order(0.98637334, 0.11037102)

  val ethereumSellOrder = Order(0.08134966, 0.1104)

  val litecoinTrade = Trade(141567635, "2018-01-31T18:05:50.923", 0.78722469, 0.016038, 0.0126255, "FILL", "BUY")
  val ethereumTrade = Trade(206517064, "2018-01-31T18:06:23.343", 0.05475369, 0.11037015, 0.00604317, "FILL", "BUY")

  val testOrderUuid = OrderUuid(value = "e606d53c-8d70-11e3-94b5-425861b86ab6")

  val testOpenOrder = OpenOrder(
    quantity = 5.00000000,
    quantityRemaining = 5.00000000,
    closed = None,
    uuid = None,
    price = 0E-8,
    orderUuid = "09aa5bb6-8232-41aa-9b78-a5a1093e0211",
    opened = "2014-07-09T03:55:48.77",
    exchange = "BTC-LTC",
    condition = None,
    pricePerUnit = None,
    isConditional = false,
    cancelInitiated = false,
    commissionPaid = 0E-8,
    orderType = "LIMIT_SELL",
    conditionTarget = None,
    limit = 2.00000000,
    immediateOrCancel = false
  )

  val testBitcoinBalance1 = Balance(
    requested = Some(false),
    uuid = None,
    balance = 14.21549076,
    cryptoAddress = Some("1Mrcdr6715hjda34pdXuLqXcju6qgwHA31"),
    currency = "BTC",
    available = 14.21549076,
    pending = 0E-8
  )

  val testBitcoinBalance2 = Balance(
    requested = Some(false),
    uuid = None,
    balance = 4.21549076,
    cryptoAddress = Some("1MacMr6715hjds342dXuLqXcju6fgwHA31"),
    currency = "BTC",
    available = 4.21549076,
    pending = 0E-8
  )

  val testCryptoAddress = CryptoAddress(
    currency = "VTC",
    address = Some("Vy5SKeKGXUHKS2WVpJ76HYuKAu3URastUo")
  )

  val testClosedOrder = ClosedOrder(
    reserved = 0.00001000,
    quantity = 1000.00000000,
    quantityRemaining = 1000.00000000,
    closed = None,
    price = 0E-8,
    orderUuid = "0cb4c4e4-bdc7-4e13-8c13-430e587d2cc1",
    commissionReserved = 2E-8,
    accountId = None,
    opened = "2014-07-13T07:45:46.27",
    exchange = "BTC-SHLD",
    reserveRemaining = 0.00001000,
    sentinel = "6c454604-22e2-4fb4-892e-179eede20972",
    condition = "NONE",
    commissionReserveRemaining = 2E-8,
    pricePerUnit = None,
    isConditional = false,
    cancelInitiated = false,
    commissionPaid = 0E-8,
    isOpen = true,
    orderType = "LIMIT_BUY",
    limit = 1E-8,
    immediateOrCancel = false
  )

  val testOrderHistory = OrderHistory(
    quantity = 100000.00000000,
    commission = 0E-8,
    quantityRemaining = 100000.00000000,
    price = 0E-8,
    orderUuid = "fd97d393-e9b9-4dd1-9dbf-f288fc72a185",
    timeStamp = "2014-07-09T04:01:00.667",
    exchange = "BTC-LTC",
    condition = None,
    pricePerUnit = None,
    isConditional = false,
    orderType = "LIMIT_BUY",
    conditionTarget = None,
    limit = 1E-8,
    immediateOrCancel = false
  )

  val testWithdrawalHistory = WithdrawalHistory(
    authorized = true,
    amount = 17.00000000,
    opened = "2014-07-09T04:24:47.217",
    paymentUuid = "b52c7a5c-90c6-4c6e-835c-e16df12708b1",
    txCost = 0.00020000,
    canceled = true,
    invalidAddress = false,
    currency = "BTC",
    address = "1DeaaFBdbB5nrHj87x3NHS4onvw1GPNyAu",
    pendingPayment = false,
    txId = None
  )

  val testDepositHistory = DepositHistory(
    authorized = true,
    amount = 0.00156121,
    opened = "2014-07-11T03:41:25.323",
    paymentUuid = "554ec664-8842-4fe9-b491-06225becbd59",
    txCost = 0.00020000,
    canceled = false,
    invalidAddress = false,
    currency = "BTC",
    address = "1K37yQZaGrPKNTZ5KNP792xw8f7XbXxetE",
    pendingPayment = false,
    txId = Some("70cf6fdccb9bd38e1a930e13e4ae6299d678ed6902da710fa3cc8d164f9be126")
  )
}
