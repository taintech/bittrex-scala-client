package com.taintech.bittrex

import akka.actor.{ ActorSystem, Cancellable }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.taintech.bittrex.client.BittrexClient
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

object Main extends App with LazyLogging {

  implicit val system: ActorSystem                        = ActorSystem("bittrex-system")
  implicit val materializer: ActorMaterializer            = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  logger.info("Application started.")

  implicit val timeout: FiniteDuration = 2 seconds

  val bittrexClient: BittrexClient = BittrexClient()

  val graph = Source
    .tick(1 second, 2 second, "tick")
    .mapAsync(1)(_ => bittrexClient.getMarketSummaries)
    .map(_.toString())
    .to(Sink.foreach(s => logger.info(s)))

  val cancellable: Cancellable = graph.run()

  logger.info(s"Bittrex client started\nPress RETURN to stop...")
  StdIn.readLine()
  cancellable.cancel()
  system.terminate()
  Thread.sleep(100)
  logger.info(s"Stopping application.")
  System.exit(0)
}
