package com.taintech.bittrex.client

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.util.Random
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class BittrexClientSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with BittrexTestServer {

  import BittrexClientSpec._

  "BittrexClient" should {
    "load markets" ignore new BittrexClientFixture("public-api/getmarkets.json") {
      val testClient = BittrexClient(testConf)
      whenReady(testClient.getMarkets) { result =>
        result should not be empty
      }
    }
  }

  class BittrexClientFixture(resource: String) {}

  val hostname: String = host
  val port: Int = testServerPort

  val publicApiRoutes: Route = reject
}

object BittrexClientSpec {

  val host = "localhost"
  val testServerPort: Int = 9000 + (new Random).nextInt(100)
  val bufferSize = 10
  val rootUrl = "/test"
  val publicApiUrl = "/test/public"
  val testConf =
    BittrexClientConfig(host, testServerPort, rootUrl, publicApiUrl, bufferSize)

}
