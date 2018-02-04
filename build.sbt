name := "bittrex-client"

organization := "com.github.taintech"

homepage := Some(url("https://github.com/taintech/bittrex-scala-client"))

licenses := Seq(
  "MIT" -> url(
    "https://github.com/taintech/bittrex-scala-client/blob/master/LICENSE"))

version := "0.2"

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
  "com.github.pureconfig" %% "pureconfig" % "0.9.0",
  "org.scalactic" %% "scalactic" % "3.0.4" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.20" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.20" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

developers := List(
  Developer(id = "taintech",
            name = "Rinat Tainov",
            email = "rinattainov@gmail.com.com",
            url = new URL(s"http://github.com/taintech"))
)
scmInfo := Some(
  ScmInfo(url(s"https://github.com/taintech/bittrex-scala-client"),
          s"git@github.com:taintech/bittrex-scala-client.git"))
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := true
publishArtifact in Test := false
publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
  else Opts.resolver.sonatypeStaging)

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield
  Credentials("Sonatype Nexus Repository Manager",
              "oss.sonatype.org",
              username,
              password)).toSeq
