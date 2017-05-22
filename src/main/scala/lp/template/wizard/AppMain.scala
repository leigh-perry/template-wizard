package lp.template.wizard

import java.io.File

import com.twitter.app.{App, Flag}
import lp.template.common.Apps

object AppMain extends App {
  val templateDir: Flag[String] = flag("-template-dir", "Directory to use as template")
  val destinationDir: Flag[String] = flag("-destination-dir", "Destination directory")
  val substitutions: Flag[String] =
    flag("-substitutions", "Comma-delimited pairs eg projctPackage=au.com.leighperry,projctName=someApp")

  def main(): Unit = {
    try {
      runApp()
    }
    catch {
      case e: Exception =>
        println(Apps.getExceptionDetails(e))
    }
  }

  private def runApp() = {
    val inputDir = templateDir()
    val destinationDirectory = destinationDir()
    val substs: Array[(String, String)] = splitPairs(substitutions())

    val inputDirLength = inputDir.length

    val files = recursiveFileObjects(new File(inputDir))
    val filesFromTo =
      files.map {
        file => {
          val inputFilepath = file.getCanonicalPath
          val inputFilepathRelative = inputFilepath.substring(inputDirLength)
          val parentPath = file.getParentFile.getCanonicalPath
          val filename = file.getName

          val outputPathRelativeDir =
            if (parentPath.length > inputDirLength) {
              val inputPathRelativeDir = parentPath.substring(inputDirLength)
              s"$destinationDirectory/$inputPathRelativeDir/$filename"
            } else {
              s"$destinationDirectory/$filename"
            }

          val outputPathRelativeDirExpanded = expand(substs, outputPathRelativeDir)
          println((inputFilepath, outputPathRelativeDirExpanded))
          (inputFilepath, outputPathRelativeDirExpanded)
        }
      }
  }

  def splitPairs(s: String): Array[(String, String)] = {
    s.trim match {
      case "" => Array()
      case trimmed@_ =>
        trimmed.split(",")
          .map(fromTo(_))
    }
  }

  def fromTo(s: String): (String, String) = {
    val trimmed = s.trim
    val terms = trimmed.split("=")
    if (terms.length != 2) {
      throw new RuntimeException(s"$s is not a valid term... must be of the form 'from=to'")
    }

    (terms(0), terms(1))
  }

  val dirIgnoreSuffixes = Array(".git")
  val fileIgnoreSuffixes = Array(".DS_Store")

  def recursiveFileObjects(dir: File): List[File] = {
    var result = List[File]()

    val filesAndDirs = dir.listFiles
    if (filesAndDirs == null) {
      throw new RuntimeException(s"Invalid directory $dir")
    }

    for (file <- filesAndDirs) {
      val path = file.getCanonicalPath
      if (!file.isFile) {
        // Must be a directory - recurse
        if (!shouldIgnore(path, dirIgnoreSuffixes)) {
          result = result ::: recursiveFileObjects(file)
        }
      }
      else if (!shouldIgnore(path, fileIgnoreSuffixes)) {
        result = result :+ file
      }
    }

    result
  }

  def shouldIgnore(path: String, ignoreSuffixes: Array[String]): Boolean = {
    for (suffix <- ignoreSuffixes)
      if (path.endsWith(s"/$suffix")) {
        return true
      }

    false
  }

  def expand(substs: Array[(String, String)], s: String) = {
    val substVariants =
      substs
        .flatMap(variantsOf(_))

    substVariants.foldLeft(s) {
      case (result, (from, to)) =>
        result.replaceAll(from, to)
    }
  }

  def variantsOf(fromTo: (String, String)): Array[(String, String)] = {
    val from = fromTo._1
    val to = fromTo._2

    val lowerCase = (from.toLowerCase, to.toLowerCase)
    val upperCase = (from.toUpperCase, to.toUpperCase)
    val snakeCase = (Ascii.classToSnakeCase(from), Ascii.classToSnakeCase(to))
    val snakeUpperCase = (Ascii.classToSnakeUpperCase(from), Ascii.classToSnakeUpperCase(to))
    val snakeMinusCase = (Ascii.classToMinusSnakeCase(from), Ascii.classToMinusSnakeCase(to))
    val methodCase = (Ascii.classToMethodCase(from), Ascii.classToMethodCase(to))
    Array(fromTo, lowerCase, upperCase, snakeCase, snakeUpperCase, snakeMinusCase, methodCase)
  }
}

