package utility

import java.io.{ PrintWriter, StringWriter }

case class ExceptionLogger(base: Exception) {
  val printableStackTrace: String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    base.printStackTrace(pw)
    sw.toString()
  }
}
