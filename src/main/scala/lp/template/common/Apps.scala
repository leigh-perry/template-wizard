package lp.template.common

import java.io.{PrintWriter, StringWriter, Writer}


object Apps {
  def getExceptionDetails(e: Throwable): String = {
    val stackTrace: String = getStackTrace(e)
    val s: String = "Exception details:%n  %s".format(stackTrace)

    // Remove trailing newline.
    s.trim
  }

  def getStackTrace(e: Throwable): String = {
    val sw: Writer = new StringWriter
    val pw: PrintWriter = new PrintWriter(sw)
    e.printStackTrace(pw)
    sw.toString
  }
}
