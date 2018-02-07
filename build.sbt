name := "bittrex-client"

organization := "com.github.taintech"

version := "0.3-SNAPSHOT"

scalaVersion := "2.12.4"

mainClass in (Compile, run) := Some("com.taintech.bittrex.Main")

val akkaVersion      = "2.5.9"
val akkaHTTPVersion  = "10.1.0-RC1"
val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "ch.qos.logback"             % "logback-classic"      % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"       % "3.7.2",
  "com.typesafe.akka"          %% "akka-actor"          % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream"         % akkaVersion,
  "com.typesafe.akka"          %% "akka-http"           % akkaHTTPVersion,
  "io.argonaut"                %% "argonaut"            % "6.2.1",
  "org.bouncycastle"           % "bcpkix-jdk15on"       % "1.59",
  "com.github.pureconfig"      %% "pureconfig"          % "0.9.0",
  "org.scalactic"              %% "scalactic"           % scalaTestVersion % Test,
  "com.typesafe.akka"          %% "akka-testkit"        % akkaVersion % Test,
  "com.typesafe.akka"          %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka"          %% "akka-http-testkit"   % akkaHTTPVersion % Test,
  "org.scalatest"              %% "scalatest"           % scalaTestVersion % Test
)

val username     = "taintech"
val repo         = "bittrex-scala-client"
val usernameRepo = s"$username/$repo"

homepage := Some(url(s"https://github.com/$usernameRepo"))

developers := List(
  Developer(
    id = username,
    name = "Rinat Tainov",
    email = "rinattainov@gmail.com.com",
    url = new URL(s"http://github.com/$username")
  )
)

scmInfo := Some(ScmInfo(url(s"https://github.com/$usernameRepo"), s"git@github.com:$usernameRepo.git"))
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := true
publishArtifact in Test := false
publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
  else Opts.resolver.sonatypeStaging
)

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

licenses := Seq("MIT" -> url(s"https://github.com/$usernameRepo/blob/master/LICENSE"))
