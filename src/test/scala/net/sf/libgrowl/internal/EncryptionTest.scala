package net.sf.libgrowl.internal

import org.scalatest.FunSuite

class EncryptionTest extends FunSuite {
  test("get salt"){
    assert(Encryption.getSalt !== null)
  }
}
