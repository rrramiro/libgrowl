package net.sf.libgrowl.internal

import net.sf.libgrowl.EncryptionAlgorithm
import org.scalatest.FunSuite

class EncryptionTest extends FunSuite {
  private val saltGenerator: Array[Byte] = ">>>>>>>>".getBytes(Message.ENCODING) //3E3E3E3E3E3E3E3E

  test("Encryption 1"){
    val enc = Encryption("password", EncryptionAlgorithm.DES, saltGenerator = saltGenerator)
    assert(enc.toString === "DES:8CC223B5FB517994 SHA512:17283BD06EAF0CA8D60E56975B2683BEE3FFA9C3573178871481DD3A86EC2CD9DC736B4AA7A69FC9D4548297A7E727C3FF72ADF3B1DD7657DC72C5A98BDAC6D0.3E3E3E3E3E3E3E3E")
  }

  test("Encryption 2"){
    val enc = Encryption("password", EncryptionAlgorithm.AES, saltGenerator = saltGenerator)
    assert(enc.toString === "AES:9791EF6124406C42CFC09B18E09573F8 SHA512:17283BD06EAF0CA8D60E56975B2683BEE3FFA9C3573178871481DD3A86EC2CD9DC736B4AA7A69FC9D4548297A7E727C3FF72ADF3B1DD7657DC72C5A98BDAC6D0.3E3E3E3E3E3E3E3E")
  }

  test("Encryption 3"){
    val enc = Encryption("password", EncryptionAlgorithm.DESede, saltGenerator = saltGenerator)
    assert(enc.toString === "3DES:8CC223B5FB517994AB0415769B570B670194C82591168ABA SHA512:17283BD06EAF0CA8D60E56975B2683BEE3FFA9C3573178871481DD3A86EC2CD9DC736B4AA7A69FC9D4548297A7E727C3FF72ADF3B1DD7657DC72C5A98BDAC6D0.3E3E3E3E3E3E3E3E")
  }
}
