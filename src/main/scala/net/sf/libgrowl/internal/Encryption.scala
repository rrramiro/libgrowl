package net.sf.libgrowl.internal

import java.security.{MessageDigest, SecureRandom}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}
import javax.crypto.spec._

import net.sf.libgrowl.{EncryptionAlgorithm, HashAlgorithm}
import net.sf.libgrowl.internal.Encryption.EncryptionType

object Encryption {

  val DEFAULT_RANDOM_SALT_ALGORITHM: String = "SHA1PRNG"
  val DEFAULT_SALT_SIZE: Int = 16
  val DEFAULT_TRANSFORMATION: String = "CBC/PKCS5Padding"
  val NONE_ENCRYPTION_ALGORITHM: String = "NONE"
  val BINARY_HASH_FUNCTION: String = "MD5"

  /**
   * get MD5 hash of input value
   *
   * @param input
   * @return MD5 hash as byte array or <code>null</code>
   */
  def md5(input: Array[Byte]): String = {
    val md5 = MessageDigest.getInstance(BINARY_HASH_FUNCTION)
    md5.reset()
    md5.update(input)
    toHexadecimal(md5.digest)
  }

  def toHexadecimal(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString.toUpperCase
  }

  def fromHexadecimal(hex: String): Array[Byte] = {
    assert(hex.length % 2 == 0, s"Invalid hex string [$hex]")
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  type EncryptionType = (Array[Byte] => Array[Byte])

  object NONE extends EncryptionType {
    override def apply(in: Array[Byte]): Array[Byte] = in
    override def toString: String = Encryption.NONE_ENCRYPTION_ALGORITHM
  }

  def getSalt(randomSaltAlgorithm: String = DEFAULT_RANDOM_SALT_ALGORITHM, saltSize: Int = DEFAULT_SALT_SIZE, seedGenerator: => Long = System.currentTimeMillis()): Array[Byte] = {
    val random = SecureRandom.getInstance(randomSaltAlgorithm)
    random.setSeed(seedGenerator)
    val saltArray: Array[Byte] = new Array[Byte](saltSize)
    random.nextBytes(saltArray)
    saltArray
  }

  def apply(
    passphrase: String,
    algorithm: EncryptionAlgorithm.Value = EncryptionAlgorithm.DES,
    keyHashAlgorithm: HashAlgorithm.Value = HashAlgorithm.SHA512,
    saltGenerator: => Array[Byte] = Encryption.getSalt()
  ): Encryption = {
    val salt = saltGenerator
    val hash = hashWithAlgorithm(keyHashAlgorithm) _
    val passphraseBytes = passphrase.getBytes(Message.ENCODING)
    val keyBasis = passphraseBytes ++ salt
    val key: Array[Byte] = hash(keyBasis)
    val (secretKey, iv) = getSecretKeyAndIv(algorithm, key)
    val cipher: Cipher = Cipher.getInstance(s"$algorithm/${Encryption.DEFAULT_TRANSFORMATION}")
    new Encryption(salt, hash(key), secretKey, cipher, iv, algorithm, keyHashAlgorithm)
  }

  private def getSecretKeyAndIv(algorithm: EncryptionAlgorithm.Value, key: Array[Byte]): (SecretKey, IvParameterSpec) = algorithm match {
    case EncryptionAlgorithm.DES =>
      val secretKey = SecretKeyFactory.getInstance(algorithm.toString).generateSecret(new DESKeySpec(key))
      secretKey -> new IvParameterSpec(secretKey.getEncoded)
    case EncryptionAlgorithm.DESede =>
      val secretKey = SecretKeyFactory.getInstance(algorithm.toString).generateSecret(new DESedeKeySpec(key))
      secretKey -> new IvParameterSpec(secretKey.getEncoded, 0, 8)
    case EncryptionAlgorithm.AES =>
      val secretKey = new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(null, key, 65536, 128)).getEncoded, "AES")
      secretKey -> new IvParameterSpec(secretKey.getEncoded)
  }

  private def hashWithAlgorithm(keyHashAlgorithm: HashAlgorithm.Value)(keyToUse: Array[Byte]): Array[Byte] = {
    MessageDigest.getInstance(keyHashAlgorithm.toString).digest(keyToUse)
  }
}

class Encryption(
  salt: Array[Byte],
  keyHashed: Array[Byte],
  secretKey: SecretKey,
  cipher: Cipher,
  iv: IvParameterSpec,
  algorithm: EncryptionAlgorithm.Value,
  keyHashAlgorithm: HashAlgorithm.Value
) extends EncryptionType {

  override def apply(in: Array[Byte]): Array[Byte] = {
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    cipher.doFinal(in)
  }

  override def toString: String = {
    s"${algorithm.code}:${Encryption.toHexadecimal(iv.getIV)} ${keyHashAlgorithm.toString.replaceAll("-", "")}:${Encryption.toHexadecimal(keyHashed)}.${Encryption.toHexadecimal(salt)}"
  }
}
