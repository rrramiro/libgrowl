package net.sf.libgrowl.internal

import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, SecureRandom}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}
import javax.crypto.spec.{DESKeySpec, IvParameterSpec}

object Encryption {
  /**
   * get MD5 hash of input value
   *
   * @param input
   * @return MD5 hash as byte array or <code>null</code>
   */
  def md5(input: Array[Byte]): String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(input)
    toHexadecimal(md5.digest)
  }

  def toHexadecimal(bytes: Array[Byte]): String = {
    bytes.map { md5Byte => Integer.toHexString(0xFF & md5Byte) }.mkString
  }

  object NONE extends Encryption{
    override def apply(in: Array[Byte]): Array[Byte] = in
    override def toString: String = Encryption.NONE_ENCRYPTION_ALGORITHM
  }

  val DEFAULT_RANDOM_SALT_ALGORITHM: String = "SHA1PRNG"
  val DEFAULT_SALT_SIZE: Int = 16
  val DEFAULT_KEY_HASH_ALGORITHM: String = "SHA-512"
  val DEFAULT_ALGORITHM: String = "DES"
  val DEFAULT_TRANSFORMATION: String = "DES/CBC/PKCS5Padding"
  val NONE_ENCRYPTION_ALGORITHM: String = "NONE"
  val BINARY_HASH_FUNCTION: String = "MD5"

  def getSalt: Array[Byte] = {
    val random = SecureRandom.getInstance(DEFAULT_RANDOM_SALT_ALGORITHM)
    random.setSeed(getSeed)
    val saltArray: Array[Byte] = new Array[Byte](DEFAULT_SALT_SIZE)
    random.nextBytes(saltArray)
    saltArray
  }

  protected def getSeed: Long = System.currentTimeMillis()

  def apply(passphrase: String): Encryption = {
    val salt: Array[Byte] = Encryption.getSalt
    val key: Array[Byte] = hash(passphrase.getBytes(StandardCharsets.UTF_8) ++ salt)
    val secretKey: SecretKey = SecretKeyFactory.getInstance(Encryption.DEFAULT_ALGORITHM).generateSecret(new DESKeySpec(key))
    val iv: IvParameterSpec = new IvParameterSpec(secretKey.getEncoded)
    new EncryptionPassphrase(salt, hash(key), secretKey, iv)
  }

  private def hash(keyToUse: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance(Encryption.DEFAULT_KEY_HASH_ALGORITHM).digest(keyToUse)
  }
}

trait Encryption extends ((Array[Byte]) => Array[Byte])


class EncryptionPassphrase(
  val salt: Array[Byte],
  val keyHashed: Array[Byte],
  val secretKey: SecretKey,
  val iv: IvParameterSpec
) extends Encryption {

  private val cipher = Cipher.getInstance(Encryption.DEFAULT_TRANSFORMATION)

  override def apply(in: Array[Byte]): Array[Byte] = {
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
    cipher.doFinal(in)
  }

  override def toString: String =
    s"${Encryption.DEFAULT_ALGORITHM}:${Encryption.toHexadecimal(iv.getIV)} ${Encryption.DEFAULT_KEY_HASH_ALGORITHM.replaceAll("-", "")}:${Encryption.toHexadecimal(keyHashed)}.${Encryption.toHexadecimal(salt)}"
}
