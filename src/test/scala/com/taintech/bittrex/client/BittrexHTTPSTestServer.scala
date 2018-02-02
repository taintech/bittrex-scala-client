package com.taintech.bittrex.client

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpResponse,
  StatusCodes
}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.ExecutionContextExecutor

trait BittrexHTTPSTestServer extends BeforeAndAfterAll with ScalaFutures {

  this: Suite =>

  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  protected val hostname: String
  protected val port: Int

  protected val publicApiRoutes: Route

  val https: HttpsConnectionContext = {
    val password: Array[Char] = "just4test".toCharArray

    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    val keystore: InputStream =
      getClass.getClassLoader.getResourceAsStream("just4test.p12")

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory =
      KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers,
                    tmf.getTrustManagers,
                    new SecureRandom)
    ConnectionContext.https(sslContext)
  }

  override protected def beforeAll(): Unit = {
    Http()
      .bindAndHandle(publicApiRoutes, hostname, port, connectionContext = https)
      .futureValue
  }

  override protected def afterAll(): Unit = {
    system.terminate().futureValue
  }

}
