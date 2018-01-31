package com.taintech.bittrex.client

import com.taintech.bittrex.client

case class BittrexResponse[T](
    success: Boolean,
    message: String,
    result: Option[T]
)

case class Market(marketCurrency: String,
                  baseCurrency: String,
                  marketCurrencyLong: String,
                  baseCurrencyLong: String,
                  minTradeSize: BigDecimal,
                  marketName: String,
                  isActive: Boolean,
                  created: String,
                  notice: Option[String],
                  isSponsored: Option[Boolean],
                  logoUrl: Option[String])

case class CurrencyInfo(currency: String,
                        currencyLong: String,
                        minConfirmation: BigDecimal,
                        txFee: BigDecimal,
                        isActive: Boolean,
                        coinType: String,
                        baseAddress: Option[String],
                        notice: Option[String])

case class Ticker(bid: BigDecimal, ask: BigDecimal, last: BigDecimal)

case class MarketSummary(
    marketName: String,
    high: BigDecimal,
    low: BigDecimal,
    volume: BigDecimal,
    last: BigDecimal,
    baseVolume: BigDecimal,
    timeStamp: String,
    bid: BigDecimal,
    ask: BigDecimal,
    openBuyOrders: BigDecimal,
    openSellOrders: BigDecimal,
    prevDay: BigDecimal,
    created: String,
    displayMarketName: Option[String]
)

case class Order(
    quantity: BigDecimal,
    rate: BigDecimal
)

case class OrderBook(buyOrders: List[Order], sellOrders: List[Order])

object OrderBookType extends Enumeration {
  type OrderBookType = Value
  val Buy: client.OrderBookType.Value = Value("buy")
  val Sell: client.OrderBookType.Value = Value("sell")
  val Both: client.OrderBookType.Value = Value("both")
}

case class Trade(
    id: BigDecimal,
    timestamp: String,
    quantity: BigDecimal,
    price: BigDecimal,
    total: BigDecimal,
    fillType: String,
    orderType: String
)

case class OrderUuid(
    value: String
)

case class OpenOrder(
    uuid: Option[String],
    orderUuid: String,
    exchange: String,
    orderType: String,
    quantity: BigDecimal,
    quantityRemaining: BigDecimal,
    limit: BigDecimal,
    commissionPaid: BigDecimal,
    price: BigDecimal,
    pricePerUnit: Option[BigDecimal],
    opened: String,
    closed: Option[BigDecimal],
    cancelInitiated: Boolean,
    immediateOrCancel: Boolean,
    isConditional: Boolean,
    condition: Option[String],
    conditionTarget: Option[String]
)

case class Balance(
    currency: String,
    balance: BigDecimal,
    available: BigDecimal,
    pending: BigDecimal,
    cryptoAddress: Option[String],
    requested: Option[Boolean],
    uuid: Option[String]
)

case class CryptoAddress(
    currency: String,
    address: Option[String]
)

case class ClosedOrder(
    accountId: Option[String],
    orderUuid: String,
    exchange: String,
    orderType: String,
    quantity: BigDecimal,
    quantityRemaining: BigDecimal,
    limit: BigDecimal,
    reserved: BigDecimal,
    reserveRemaining: BigDecimal,
    commissionReserved: BigDecimal,
    commissionReserveRemaining: BigDecimal,
    commissionPaid: BigDecimal,
    price: BigDecimal,
    pricePerUnit: Option[BigDecimal],
    opened: String,
    closed: Option[String],
    isOpen: Boolean,
    sentinel: String,
    cancelInitiated: Boolean,
    immediateOrCancel: Boolean,
    isConditional: Boolean,
    condition: String //ignoring "ConditionTarget", no support more than 22 attributes, can be done by merge later on
)

case class OrderHistory(
    orderUuid: String,
    exchange: String,
    timeStamp: String,
    orderType: String,
    limit: BigDecimal,
    quantity: BigDecimal,
    quantityRemaining: BigDecimal,
    commission: BigDecimal,
    price: BigDecimal,
    pricePerUnit: Option[BigDecimal],
    isConditional: Boolean,
    condition: Option[String],
    conditionTarget: Option[String],
    immediateOrCancel: Boolean
)

case class Withdrawal(
    paymentUuid: String,
    currency: String,
    amount: BigDecimal,
    address: String,
    opened: String,
    authorized: Boolean,
    pendingPayment: Boolean,
    txCost: BigDecimal,
    txId: String,
    canceled: Boolean,
    invalidAddress: Boolean
)

case class Deposit(
    paymentUuid: String,
    currency: String,
    amount: BigDecimal,
    address: String,
    opened: String,
    authorized: Boolean,
    pendingPayment: Boolean,
    txCost: BigDecimal,
    txId: String,
    canceled: Boolean,
    invalidAddress: Boolean
)
