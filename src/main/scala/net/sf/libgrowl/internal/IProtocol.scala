package net.sf.libgrowl.internal

/**
  * @author Bananeweizen
  *
  */
object IProtocol {
  /**
    * no encryption
    */
  val ENCRYPTION_NONE = "NONE"
  /**
    * GNTP version
    */
  val GNTP_VERSION = "GNTP/1.0"

  /**
    * line break for protocol lines
    */
  val LINE_BREAK = "\r\n"
  /**
    * message type NOTIFICATION
    */
  val MESSAGETYPE_NOTIFY = "NOTIFY"
  /**
    * message type REGISTER
    */
  val MESSAGETYPE_REGISTER = "REGISTER"

  /**
    * default port for communication with Growl
    */
  val DEFAULT_GROWL_PORT = 23053
  /**
    * binary data identifier
    */
  val X_GROWL_RESOURCE = "x-growl-resource://"

}
