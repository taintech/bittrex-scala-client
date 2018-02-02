package com.taintech.bittrex.client

import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.StreamConverters
import org.scalatest.time.{Millis, Seconds, Span}

import scala.language.postfixOps

class BittrexClientSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with BittrexHTTPSTestServer {

  import BittrexClientSpec._

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(2, Seconds)), interval = scaled(Span(5, Millis)))


  "BittrexClient" should {
    "load markets" in {
      val testClient = BittrexClient(testConf)
      whenReady(testClient.getMarkets) { result =>
        result should not be empty
      }
    }
    "load currencies" in {
      val testClient = BittrexClient(testConf)
      whenReady(testClient.getCurrencies) { result =>
        result should not be empty
      }
    }
  }

  override val hostname: String = host
  override val port: Int = testServerPort

  override val publicApiRoutes: Route =
    (get & path("test" / "public" / "getmarkets")) {
      complete(
        HttpResponse(
          status = StatusCodes.OK,
          headers = Nil,
          entity = HttpEntity.Chunked(
            contentType = ContentTypes.`application/json`,
            chunks = StreamConverters
              .fromInputStream(() =>
                getClass.getClassLoader.getResourceAsStream(
                  "public-api/getmarkets.json"))
              .map(ChunkStreamPart.apply)
          )
        ))
    } ~ (get & path("test" / "public" / "getcurrencies")) {
      complete(
        HttpResponse(
          status = StatusCodes.OK,
          headers = Nil,
          entity = HttpEntity.Chunked(
            contentType = ContentTypes.`application/json`,
            chunks = StreamConverters
              .fromInputStream(() =>
                getClass.getClassLoader.getResourceAsStream(
                  "public-api/getcurrencies.json"))
              .map(ChunkStreamPart.apply)
          )
        ))
    }

}

object BittrexClientSpec {

  val host = "localhost"
  val testServerPort: Int = 9069
  val bufferSize = 10
  val rootUrl = "/test"
  val publicApiUrl = "/test/public"
  val testConf =
    BittrexClientConfig(host, testServerPort, rootUrl, publicApiUrl, bufferSize)

}
