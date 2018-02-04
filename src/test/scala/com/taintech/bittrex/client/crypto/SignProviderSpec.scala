package com.taintech.bittrex.client.crypto

import org.scalatest.{Matchers, WordSpec}

class SignProviderSpec extends WordSpec with Matchers with SignProvider {

  "SignProvider" should {
    "sign a test data" in {
      val result = sign("test data", "test secret")
      result should include("ebbeb")
    }
  }

}
