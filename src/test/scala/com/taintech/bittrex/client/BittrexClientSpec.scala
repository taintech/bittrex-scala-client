package com.taintech.bittrex.client

import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpResponse,
  StatusCodes
}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.stream.scaladsl.StreamConverters
import org.scalatest.time.{Millis, Seconds, Span}

import scala.language.postfixOps

class BittrexClientSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with BittrexHTTPSTestServer {

  import BittrexClientSpec._

  val testClient = BittrexClient(testConf)

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)),
                   interval = scaled(Span(5, Millis)))

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
      whenReady(testClient.getOrderBook("BTC-LTC", OrderBookType.Both)) {
        result =>
          result.buyOrders should not be empty
          result.sellOrders should not be empty
          result.buyOrders should have size 100
          result.sellOrders should have size 100
          result.buyOrders should contain(litecoinBuyOrder)
          result.sellOrders should contain(litecoinSellOrder)
      }
    }
    "return sell order book for Litecoin" in {
      whenReady(testClient.getOrderBook("BTC-LTC", OrderBookType.Sell)) {
        result =>
          result.buyOrders should be(List.empty)
          result.sellOrders should not be empty
          result.sellOrders should have size 500
          result.sellOrders should contain(litecoinSellOrder)
      }
    }
    "return buy order book for Litecoin" in {
      whenReady(testClient.getOrderBook("BTC-LTC", OrderBookType.Buy)) {
        result =>
          result.buyOrders should not be empty
          result.sellOrders should be(List.empty)
          result.buyOrders should have size 500
          result.buyOrders should contain(litecoinBuyOrder)
      }
    }
    "return both types of order book for Ethereum" in {
      whenReady(testClient.getOrderBook("BTC-ETH", OrderBookType.Both)) {
        result =>
          result.buyOrders should not be empty
          result.sellOrders should not be empty
          result.buyOrders should have size 100
          result.sellOrders should have size 100
          result.buyOrders should contain(ethereumBuyOrder)
          result.sellOrders should contain(ethereumSellOrder)
      }
    }
    "return failed exception for bla order book" in {
      whenReady(testClient.getOrderBook("bla-bla", OrderBookType.Both).failed) {
        exception =>
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

  }

  override val hostname: String = host
  override val port: Int = testServerPort

  private def resourceRoute(url: String,
                            resource: String,
                            expectedParams: Map[String, String] =
                              Map.empty[String, String]) =
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
                  .fromInputStream(() =>
                    getClass.getClassLoader.getResourceAsStream(resource))
                  .map(ChunkStreamPart.apply)
              )
            ))
        } else reject
      }
    }

  private def publicApiRoute(url: String,
                             resource: String,
                             expectedParams: Map[String, String] =
                               Map.empty[String, String]) =
    resourceRoute(s"test/public/$url", s"public-api/$resource", expectedParams)

  val publicApiRoutes: Route = publicApiRoute("getmarkets", "getmarkets.json") ~
    publicApiRoute("getcurrencies", "getcurrencies.json") ~
    publicApiRoute("getmarketsummaries", "getmarketsummaries.json") ~
    publicApiRoute("getticker",
                   "getticker-btc-ltc.json",
                   Map("market" -> "BTC-LTC")) ~
    publicApiRoute("getticker",
                   "getticker-btc-eth.json",
                   Map("market" -> "BTC-ETH")) ~
    publicApiRoute("getticker",
                   "getticker-bla-bla.json",
                   Map("market" -> "bla-bla")) ~
    publicApiRoute("getmarketsummary",
                   "getmarketsummary-btc-ltc.json",
                   Map("market" -> "BTC-LTC")) ~
    publicApiRoute("getmarketsummary",
                   "getmarketsummary-btc-eth.json",
                   Map("market" -> "BTC-ETH")) ~
    publicApiRoute("getmarketsummary",
                   "getmarketsummary-bla-bla.json",
                   Map("market" -> "bla-bla")) ~
    publicApiRoute("getorderbook",
                   "getorderbook-btc-ltc-both.json",
                   Map("market" -> "BTC-LTC", "type" -> "both")) ~
    publicApiRoute("getorderbook",
                   "getorderbook-btc-ltc-sell.json",
                   Map("market" -> "BTC-LTC", "type" -> "sell")) ~
    publicApiRoute("getorderbook",
                   "getorderbook-btc-ltc-buy.json",
                   Map("market" -> "BTC-LTC", "type" -> "buy")) ~
    publicApiRoute("getorderbook",
                   "getorderbook-btc-eth-both.json",
                   Map("market" -> "BTC-ETH", "type" -> "both")) ~
    publicApiRoute("getorderbook",
                   "getorderbook-bla-bla-both.json",
                   Map("market" -> "bla-bla", "type" -> "both")) ~
    publicApiRoute("getmarkethistory",
                   "getmarkethistory-btc-ltc.json",
                   Map("market" -> "BTC-LTC")) ~
    publicApiRoute("getmarkethistory",
                   "getmarkethistory-btc-eth.json",
                   Map("market" -> "BTC-ETH")) ~
    publicApiRoute("getmarkethistory",
                   "getmarkethistory-bla-bla.json",
                   Map("market" -> "bla-bla"))

}

object BittrexClientSpec {

  val host = "localhost"
  val testServerPort: Int = 9069
  val bufferSize = 10
  val rootUrl = "/test"
  val publicApiUrl = "/test/public"
  val testConf =
    BittrexClientConfig(host, testServerPort, rootUrl, publicApiUrl, bufferSize)

  val market = Market(
    marketName = "BTC-LTC",
    minTradeSize = 0.01469482,
    marketCurrency = "LTC",
    baseCurrencyLong = "Bitcoin",
    logoUrl = Some(
      "https://bittrexblobstorage.blob.core.windows.net/public/6defbc41-582d-47a6-bb2e-d0fa88663524.png"),
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

  val litecoinTrade = Trade(141567635,
                            "2018-01-31T18:05:50.923",
                            0.78722469,
                            0.016038,
                            0.0126255,
                            "FILL",
                            "BUY")
  val ethereumTrade = Trade(206517064,
                            "2018-01-31T18:06:23.343",
                            0.05475369,
                            0.11037015,
                            0.00604317,
                            "FILL",
                            "BUY")

  //Helper methods to create mock data
  def printCaseClass(caseClass: AnyRef): Unit = {

    def caseClassToMap(caseClass: AnyRef): Map[String, Any] = {
      (Map.empty[String, Any] /: caseClass.getClass.getDeclaredFields) {
        (a, f) =>
          f.setAccessible(true)
          a + (f.getName -> f.get(caseClass))
      }
    }

    println(caseClass.getClass.getSimpleName + "(")
    println(
      caseClassToMap(caseClass)
        .map {
          case (field, value) =>
            s"$field = ${if (value.isInstanceOf[String]) s""""$value""""
            else value.toString}"
        }
        .mkString(", \n"))
    println(")")
  }

}
