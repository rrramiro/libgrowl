package net.sf.libgrowl.internal

import org.scalatest.FunSuite

class EncryptionTest extends FunSuite {
  test("Encryption"){
    val salt: Array[Byte] = ">>>>>>>>".getBytes(Message.ENCODING) //3E3E3E3E3E3E3E3E
    val enc = Encryption.apply("passphrase", saltGenerator = salt)
    assert(enc.toString === "DES:E0AEEC7697460120 SHA512:FF0CBA3E7B047DA66346FDA2DBFC2F5AC07AE9647CC68427F9FC3D19B42ED6A721ADE981B04497DF38F2F031D940EE4B4D12544F26B483936AE0E08CE0931950.3E3E3E3E3E3E3E3E")
  }

  test("Encryption 2"){
    val salt: Array[Byte] = Encryption.fromHexadecimal("1C7B897F3617948632368220C6B4EC00")
    val enc = Encryption.apply("password", saltGenerator = salt)
    assert(enc.toString === "DES:9240C102E9684976 SHA512:EBB354012411936316FBC2F01A1113C3721A035BD9C1992685FCB2B13471146FFE913F0EB46C45E14D46EE6BC342D4CABE81E36EEC0C7DD42F00035AF2E7ED5D.1C7B897F3617948632368220C6B4EC00")
  }
}
