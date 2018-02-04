# Bittrex Scala Client

[![Build Status](https://travis-ci.org/taintech/bittrex-scala-client.svg?branch=master)](https://travis-ci.org/taintech/bittrex-scala-client)
<!--- [![Coverage Status](https://coveralls.io/repos/github/taintech/bittrex-scala-client/badge.svg?branch=master)](https://coveralls.io/github/taintech/bittrex-scala-client?branch=master) --->

Bittrex Scala Client is Scala library that implements fast, robust, simple and stable http client for Bittrex REST API.

## Quick Start

To use Bittrex Scala Client in an existing SBT project with Scala 2.12.4, add the following dependency to your `build.sbt`:

```scala
libraryDependencies += "com.github.taintech" %% "bittrex-client" % "0.2"
```

Create instance of http client and start hacking:
```scala
val bittrexClient = BittrexClient()
```

For a full example of project you can have a look at this [GitHub Repo](https://github.com/taintech/bittrex-scala-client-example).

Public API is available without any configuration.

Bittrex Scala Client implements all API methods under official documentation in [Bittrex API](https://bittrex.com/Home/Api)

## Market and Account API

To Use Market and Account APIs you need to create API keys under your account `Settings->Manage API Keys`. 
Then create configuration file `src/main/resources/application.conf`:
```
bittrex-client {
  host = "bittrex.com"
  port = 443
  api-path = "/api/v1.1"
  account-key = {
    api-key = "<your-api-key>"
    api-secret = "<your-api-secret>"
  }
}
```

To create orders in bittrex, don't forget to whitelist your IP address in Bittrex Settings.

## License

[MIT](LICENSE)