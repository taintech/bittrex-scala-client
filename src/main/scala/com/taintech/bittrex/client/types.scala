package com.taintech.bittrex.client

import com.taintech.bittrex.client

final case class BittrexResponse[T](
    success: Boolean,
    message: String,
    result: Option[T]
)

final case class Market(marketCurrency: String,
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

final case class CurrencyInfo(currency: String,
                              currencyLong: String,
                              minConfirmation: BigDecimal,
                              txFee: BigDecimal,
                              isActive: Boolean,
                              coinType: String,
                              baseAddress: Option[String],
                              notice: Option[String])

final case class Ticker(bid: BigDecimal, ask: BigDecimal, last: BigDecimal)

final case class MarketSummary(
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

final case class Order(
    quantity: BigDecimal,
    rate: BigDecimal
)

final case class OrderBook(buyOrders: List[Order], sellOrders: List[Order])

object OrderBookType extends Enumeration {
  type OrderBookType = Value
  val Buy: client.OrderBookType.Value  = Value("buy")
  val Sell: client.OrderBookType.Value = Value("sell")
  val Both: client.OrderBookType.Value = Value("both")
}

final case class Trade(
    id: BigDecimal,
    timestamp: String,
    quantity: BigDecimal,
    price: BigDecimal,
    total: BigDecimal,
    fillType: String,
    orderType: String
)

final case class OrderUuid(
    value: String
)

final case class OpenOrder(
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

final case class Balance(
    currency: String,
    balance: BigDecimal,
    available: BigDecimal,
    pending: BigDecimal,
    cryptoAddress: Option[String],
    requested: Option[Boolean],
    uuid: Option[String]
)

final case class CryptoAddress(
    currency: String,
    address: Option[String]
)

final case class ClosedOrder(
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

final case class OrderHistory(
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

final case class WithdrawalHistory(
    paymentUuid: String,
    currency: String,
    amount: BigDecimal,
    address: String,
    opened: String,
    authorized: Boolean,
    pendingPayment: Boolean,
    txCost: BigDecimal,
    txId: Option[String],
    canceled: Boolean,
    invalidAddress: Boolean
)

final case class DepositHistory(
    paymentUuid: String,
    currency: String,
    amount: BigDecimal,
    address: String,
    opened: String,
    authorized: Boolean,
    pendingPayment: Boolean,
    txCost: BigDecimal,
    txId: Option[String],
    canceled: Boolean,
    invalidAddress: Boolean
)
