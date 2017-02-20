package net.sf.libgrowl


object Priority extends Enumeration {
  type Priority = Value
  val LOWEST = Value(-2)
  val LOW = Value(-1)
  val NORMAL = Value(0)
  val HIGH = Value(1)
  val HIGHEST = Value(2)
}

object MessageType extends Enumeration {
  type MessageType = Value
  val REGISTER, NOTIFY, OK, CALLBACK, ERROR = Value
}

object ErrorStatus extends Enumeration {
  val RESERVED = Value(100)
  val TIMED_OUT = Value(200)
  val NETWORK_FAILURE = Value(201)
  val INVALID_REQUEST = Value(300)
  val UNKNOWN_PROTOCOL = Value(301)
  val UNKNOWN_PROTOCOL_VERSION = Value(302)
  val REQUIRED_HEADER_MISSING = Value(303)
  val NOT_AUTHORIZED = Value(400)
  val UNKNOWN_APPLICATION = Value(401)
  val UNKNOWN_NOTIFICATION = Value(402)
  val INTERNAL_SERVER_ERROR = Value(500)

}

object CallbackResult extends Enumeration {
  val CLICK, CLICKED, CLOSE, CLOSED, TIMEOUT, TIMEDOUT = Value
}

object HashAlgorithm extends Enumeration {
  val MD5 = Value
  val SHA1 = Value("SHA-256")
  val SHA384 = Value("SHA-384")
  val SHA512 = Value("SHA-512")
}

object EncryptionAlgorithm extends Enumeration {
  val DES, AES, DESede = Value
}
