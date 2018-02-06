package com.taintech.bittrex.client.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.bouncycastle.util.encoders.Hex

trait SignProvider {

  private val algorithmName = "HmacSHA512"

  private lazy val algorithmInstance: Mac = {
    Mac.getInstance(algorithmName)
  }

  protected def sign(data: String, secret: String): String = {
    val keyBytes = toBytes(secret)
    val keySpec  = new SecretKeySpec(keyBytes, algorithmName)
    algorithmInstance.init(keySpec)
    val dataBytes = algorithmInstance.doFinal(toBytes(data))
    Hex.toHexString(dataBytes)
  }

  private def toBytes(data: String) = data.getBytes("UTF-8")

}
