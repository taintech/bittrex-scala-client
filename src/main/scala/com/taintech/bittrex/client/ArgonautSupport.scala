package com.taintech.bittrex.client

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import argonaut.Argonaut._
import argonaut._

import scala.collection.immutable.Seq

trait ArgonautSupport {

  implicit val marketCodec: CodecJson[Market] =
    casecodec11(Market.apply, Market.unapply)(
      "MarketCurrency",
      "BaseCurrency",
      "MarketCurrencyLong",
      "BaseCurrencyLong",
      "MinTradeSize",
      "MarketName",
      "IsActive",
      "Created",
      "Notice",
      "IsSponsored",
      "LogoUrl"
    )

  implicit val currencyInfoCodec: CodecJson[CurrencyInfo] =
    casecodec8(CurrencyInfo.apply, CurrencyInfo.unapply)(
      "Currency",
      "CurrencyLong",
      "MinConfirmation",
      "TxFee",
      "IsActive",
      "CoinType",
      "BaseAddress",
      "Notice"
    )

  implicit val tickerCodec: CodecJson[Ticker] =
    casecodec3(Ticker.apply, Ticker.unapply)(
      "Bid",
      "Ask",
      "Last"
    )

  implicit val marketSummaryCodec: CodecJson[MarketSummary] =
    casecodec14(MarketSummary.apply, MarketSummary.unapply)(
      "MarketName",
      "High",
      "Low",
      "Volume",
      "Last",
      "BaseVolume",
      "TimeStamp",
      "Bid",
      "Ask",
      "OpenBuyOrders",
      "OpenSellOrders",
      "PrevDay",
      "Created",
      "DisplayMarketName"
    )

  implicit val orderCodec: CodecJson[Order] =
    casecodec2(Order.apply, Order.unapply)("Quantity", "Rate")

  implicit val orderBookCodec: CodecJson[OrderBook] =
    casecodec2(OrderBook.apply, OrderBook.unapply)("buy", "sell")

  implicit val tradeCodec: CodecJson[Trade] =
    casecodec7(Trade.apply, Trade.unapply)(
      "Id",
      "TimeStamp",
      "Quantity",
      "Price",
      "Total",
      "FillType",
      "OrderType"
    )

  implicit val orderUuidCodec: CodecJson[OrderUuid] =
    casecodec1(OrderUuid.apply, OrderUuid.unapply)("uuid")

  implicit val openOrderCodec: CodecJson[OpenOrder] =
    casecodec17(OpenOrder.apply, OpenOrder.unapply)(
      "Uuid",
      "OrderUuid",
      "Exchange",
      "OrderType",
      "Quantity",
      "QuantityRemaining",
      "Limit",
      "CommissionPaid",
      "Price",
      "PricePerUnit",
      "Opened",
      "Closed",
      "CancelInitiated",
      "ImmediateOrCancel",
      "IsConditional",
      "Condition",
      "ConditionTarget",
    )

  implicit val balanceCodec: CodecJson[Balance] =
    casecodec7(Balance.apply, Balance.unapply)(
      "Currency",
      "Balance",
      "Available",
      "Pending",
      "CryptoAddress",
      "Requested",
      "Uuid"
    )

  implicit val cryptoAddressCodec: CodecJson[CryptoAddress] =
    casecodec2(CryptoAddress.apply, CryptoAddress.unapply)(
      "Currency",
      "Address"
    )

  implicit val closedOrderCodec: CodecJson[ClosedOrder] =
    casecodec22(ClosedOrder.apply, ClosedOrder.unapply)(
      "AccountId",
      "OrderUuid",
      "Exchange",
      "Type",
      "Quantity",
      "QuantityRemaining",
      "Limit",
      "Reserved",
      "ReserveRemaining",
      "CommissionReserved",
      "CommissionReserveRemaining",
      "CommissionPaid",
      "Price",
      "PricePerUnit",
      "Opened",
      "Closed",
      "IsOpen",
      "Sentinel",
      "CancelInitiated",
      "ImmediateOrCancel",
      "IsConditional",
      "Condition" //ignoring "ConditionTarget", no support more than 22 attributes, can be done by merge later on
    )

  implicit val orderHistoryCodecs: CodecJson[OrderHistory] =
    casecodec14(OrderHistory.apply, OrderHistory.unapply)(
      "OrderUuid",
      "Exchange",
      "TimeStamp",
      "OrderType",
      "Limit",
      "Quantity",
      "QuantityRemaining",
      "Commission",
      "Price",
      "PricePerUnit",
      "IsConditional",
      "Condition",
      "ConditionTarget",
      "ImmediateOrCancel"
    )

  implicit val withdrawalHistoryCodes: CodecJson[Withdrawal] =
    casecodec11(Withdrawal.apply, Withdrawal.unapply)(
      "PaymentUuid",
      "Currency",
      "Amount",
      "Address",
      "Opened",
      "Authorized",
      "PendingPayment",
      "TxCost",
      "TxId",
      "Canceled",
      "InvalidAddress"
    )

  implicit val depositHistoryCodesWithdrawal: CodecJson[Deposit] =
    casecodec11(Deposit.apply, Deposit.unapply)(
      "PaymentUuid",
      "Currency",
      "Amount",
      "Address",
      "Opened",
      "Authorized",
      "PendingPayment",
      "TxCost",
      "TxId",
      "Canceled",
      "InvalidAddress"
    )

  def bittrexResponseCodec[T: EncodeJson: DecodeJson]
    : CodecJson[BittrexResponse[T]] =
    casecodec3(BittrexResponse.apply[T], BittrexResponse.unapply[T])("success",
                                                                     "message",
                                                                     "result")

  implicit val withdrawalHistoryResponseCodec
    : CodecJson[BittrexResponse[List[Withdrawal]]] =
    bittrexResponseCodec[List[Withdrawal]]

  implicit val depositHistoryResponseCodec
    : CodecJson[BittrexResponse[List[Deposit]]] =
    bittrexResponseCodec[List[Deposit]]

  implicit val orderHistoryResponseCodec
    : CodecJson[BittrexResponse[List[OrderHistory]]] =
    bittrexResponseCodec[List[OrderHistory]]

  implicit val closedOrderResponseCodec
    : CodecJson[BittrexResponse[ClosedOrder]] =
    bittrexResponseCodec[ClosedOrder]

  implicit val cryptoAddressResponseCodec
    : CodecJson[BittrexResponse[CryptoAddress]] =
    bittrexResponseCodec[CryptoAddress]

  implicit val balanceResponseCodec: CodecJson[BittrexResponse[Balance]] =
    bittrexResponseCodec[Balance]

  implicit val balancesResponseCodec
    : CodecJson[BittrexResponse[List[Balance]]] =
    bittrexResponseCodec[List[Balance]]

  implicit val openOrdersCodec: CodecJson[BittrexResponse[List[OpenOrder]]] =
    bittrexResponseCodec[List[OpenOrder]]

  implicit val orderUuidResponseCodec: CodecJson[BittrexResponse[OrderUuid]] =
    bittrexResponseCodec[OrderUuid]

  implicit val marketHistoryCodec: CodecJson[BittrexResponse[List[Trade]]] =
    bittrexResponseCodec[List[Trade]]

  implicit val orderBookResponseCodec: CodecJson[BittrexResponse[OrderBook]] =
    bittrexResponseCodec[OrderBook]

  implicit val marketSummariesCodec
    : CodecJson[BittrexResponse[List[MarketSummary]]] =
    bittrexResponseCodec[List[MarketSummary]]

  implicit val marketsCodec: CodecJson[BittrexResponse[List[Market]]] =
    bittrexResponseCodec[List[Market]]

  implicit val currenciesCodec: CodecJson[BittrexResponse[List[CurrencyInfo]]] =
    bittrexResponseCodec[List[CurrencyInfo]]

  implicit val tickerResponseCodec: CodecJson[BittrexResponse[Ticker]] =
    bittrexResponseCodec[Ticker]

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller =
    Marshaller.stringMarshaller(`application/json`)

  implicit def unmarshaller[A: DecodeJson]: FromEntityUnmarshaller[A] = {
    def parse(s: String) =
      Parse.parse(s) match {
        case Right(json)   => json
        case Left(message) => sys.error(message)
      }
    def decode(json: Json) =
      implicitly[DecodeJson[A]].decodeJson(json).result match {
        case Right(entity) => entity
        case Left((m, h))  => sys.error(m + " - " + h)
      }
    jsonStringUnmarshaller.map(parse).map(decode)
  }

  implicit def marshaller[A: EncodeJson]: ToEntityMarshaller[A] =
    jsonStringMarshaller
      .compose(PrettyParams.nospace.pretty)
      .compose(implicitly[EncodeJson[A]].apply)
}
