package lp.template.common

import java.io.{PrintWriter, StringWriter, Writer}


object Apps {
  def safeFormat(format: String, arguments: Any*): String = {
    if (arguments.isEmpty) {
      format
    } else {
      val seq: Seq[AnyRef] = arguments.map(_.asInstanceOf[AnyRef])
      format.format(seq: _*)
    }
  }

  def getExceptionDetails(e: Throwable): String = {
    val stackTrace: String = getStackTrace(e)
    val s: String = "Exception details:%n  %s".format(stackTrace)

    // Remove trailing newline.
    s.trim
  }

  def pause(msec: Long): Unit = {
    try {
      Thread.sleep(msec)
    }
    catch {
      case _: InterruptedException =>
    }
  }

  def getStackTrace(e: Throwable): String = {
    val sw: Writer = new StringWriter
    val pw: PrintWriter = new PrintWriter(sw)
    e.printStackTrace(pw)
    sw.toString
  }

  def arrayToString[T](a:Array[T] ) : String = {
    s"[${a.mkString(",")}]"
  }
}
