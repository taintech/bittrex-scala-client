# Bittrex Scala Client

[![Build Status](https://travis-ci.org/taintech/bittrex-scala-client.svg?branch=master)](https://travis-ci.org/taintech/bittrex-scala-client)
<!--- [![Coverage Status](https://coveralls.io/repos/github/taintech/bittrex-scala-client/badge.svg?branch=master)](https://coveralls.io/github/taintech/bittrex-scala-client?branch=master) --->

Bittrex Scala Client is Scala library that implements fast, robust, simple and stable http client for Bittrex REST API.

## Quick Start

To use Bittrex Scala Client in an existing SBT project with Scala 2.12.4, add the following dependency to your `build.sbt`:

```scala
libraryDependencies += "com.github.taintech" %% "bittrex-client" % "0.2"
```

Create instance of http client and start hacking:
```scala
val bittrexClient = BittrexClient()
```

For a full example of project you can have a look at this [GitHub Repo](https://github.com/taintech/bittrex-scala-client-example).

Public API is available without any configuration.

Bittrex Scala Client implements all API methods under official documentation in [Bittrex API](https://bittrex.com/Home/Api)

## Market and Account API

To Use Market and Account APIs you need to create API keys under your account in Bittrex go to `Settings -> Manage API Keys`. 
Then create configuration file `src/main/resources/application.conf`:
```
bittrex-client {
  host = "bittrex.com"
  port = 443
  api-path = "/api/v1.1"
  account-key = {
    api-key = "<your-api-key>"
    api-secret = "<your-api-secret>"
  }
}
```

To create orders in bittrex, don't forget to whitelist your IP address in Bittrex Settings.

## Under the hood

Bittrex Scala Client mostly reflects all REST calls available from [Bittrex API](https://bittrex.com/Home/Api), except few modifications.
All avaiable methods are listed in `BittrexClient.scala`:
```scala
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
``` 

Mainly used dependencies are:
 - [Akka HTTP](https://doc.akka.io/docs/akka-http/current/)
 - [Akka Streams](https://doc.akka.io/docs/akka/current/guide/modules.html?language=scala#streams)
 - [Argonaut](http://argonaut.io/)
 - [PureConfig](https://pureconfig.github.io/)
 - [Bouncy Castle](https://www.bouncycastle.org/java.html)

## License

[MIT](LICENSE)