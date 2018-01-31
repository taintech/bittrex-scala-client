name := "bittrex-client"

version := "0.1"

scalaVersion := "2.12.4"

mainClass in (Compile, run) := Some("com.taintech.bittrex.Main")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe.akka" %% "akka-actor" % "2.4.20",
  "com.typesafe.akka" %% "akka-stream" % "2.4.20",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "io.argonaut" %% "argonaut" % "6.2",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.58",
  "org.scalactic" %% "scalactic" % "3.0.4" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.20" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.20" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)
