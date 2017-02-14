package net.sf.libgrowl.internal

import scala.collection.mutable

class MessageBuilder {
  val buffer: StringBuilder = new StringBuilder
  val resources: mutable.HashMap[String, Array[Byte]] = mutable.HashMap[String, Array[Byte]]()
}
