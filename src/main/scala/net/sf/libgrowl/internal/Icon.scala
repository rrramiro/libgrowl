package net.sf.libgrowl.internal

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, FileInputStream}
import javax.imageio.ImageIO
import javax.swing.ImageIcon

/**
  * abstract icon representation
  *
  * @author Bananeweizen
  *
  */
sealed trait Icon

case class UrlIcon(var mUrl: String) extends Icon

case class ResourceIcon(imageData: Array[Byte]) extends Icon {
  val resourceId: String = Encryption.md5(imageData)
}


object ResourceIcon {
  private val BEST_FORMAT = Array("PNG", "GIF", "JPEG")

  def apply(icon: ImageIcon): ResourceIcon = {
    val iconImage = new BufferedImage(icon.getIconWidth, icon.getIconWidth, BufferedImage.TYPE_INT_ARGB)
    val g2 = iconImage.createGraphics
    icon.paintIcon(null, g2, 0, 0)
    g2.dispose()
    val outStream = new ByteArrayOutputStream
    ImageIO.write(iconImage, getBestFormat(ImageIO.getWriterFormatNames), outStream)
    val mImageData = outStream.toByteArray
    outStream.close()
    new ResourceIcon(mImageData)
  }

  def apply(iconFile: File): ResourceIcon = {
    if (iconFile.canRead) {
      val stream = new FileInputStream(iconFile)
      val mImageData = new Array[Byte](iconFile.length.toInt)
      stream.read(mImageData)
      stream.close()
      new ResourceIcon(mImageData)
    } else {
      throw new Exception(s"Can not read file: ${iconFile.getAbsolutePath}")
    }
  }

  private def getBestFormat(formatNames: Array[String]): String = {
    ResourceIcon.BEST_FORMAT.find { formatNames.contains }.getOrElse(formatNames(0))
  }
}

