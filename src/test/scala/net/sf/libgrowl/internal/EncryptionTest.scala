package net.sf.libgrowl.internal

import java.nio.charset.StandardCharsets

import org.scalatest.FunSuite

class EncryptionTest extends FunSuite {
  test("get salt"){
    val salt: Array[Byte] = ">>>>>>>>".getBytes(StandardCharsets.UTF_8) //3E3E3E3E3E3E3E3E
    val enc = Encryption.apply("passphrase", salt)
    println(enc.toString)


  }
}
