package com.taintech.bittrex.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.ExecutionContextExecutor

trait BittrexTestServer extends BeforeAndAfterAll with ScalaFutures {

  this: Suite =>

  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  protected val hostname: String
  protected val port: Int

  protected val publicApiRoutes: Route

  override protected def beforeAll(): Unit = {
    Http()
      .bindAndHandle(publicApiRoutes, hostname, port)
      .futureValue
  }

  override protected def afterAll(): Unit = {
    system.terminate().futureValue
  }

}
