package lp.template.wizard

import java.io.File
import java.nio.file.Paths

import com.twitter.app.{App, Flag}
import fs2.{Task, io, text}
import lp.template.common.Apps

import scala.collection.mutable

object AppMain extends App {
  val templateDir: Flag[String] = flag("-template-dir", "Directory to use as template")
  val destinationDir: Flag[String] = flag("-destination-dir", "Destination directory")
  val substitutions: Flag[String] =
    flag("-substitutions", "Comma-delimited pairs eg projctPackage=au.com.leighperry,projctName=someApp")

  def main(): Unit = {
    try {
      // Build tasks to execute
      val (substVariants, tasks) = executionPlan

      println("translating...")
      substVariants
        .foreach {
          case (from, to) =>
            println(s"  $from => $to")
        }

      // Execute them
      println("generating...")
      tasks.foreach {
        case (filepath, step) => {
          step.unsafeRun()
          println(s"  $filepath")
        }
      }
    }
    catch {
      case e: Exception =>
        println(Apps.getExceptionDetails(e))
    }
  }

  private def executionPlan: (Array[(String, String)], Seq[(String, Task[Unit])]) = {
    val inputDir = templateDir()
    val destinationDirectory = destinationDir()
    val substs: Array[(String, String)] = splitPairs(substitutions())

    val substVariants: Array[(String, String)] = variantsOf(substs)

    val inputDirFile = new File(inputDir)
    val parentDir = inputDirFile.getParent
    val inputDirLength = parentDir.length + 1

    // Get list of input filepaths plus their output filepaths
    val files = recursiveFileObjects(inputDirFile)
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

          // Output filepath is
          val outputPathRelativeDirExpanded = expand(substVariants, outputPathRelativeDir)
          (inputFilepath, outputPathRelativeDirExpanded)
        }
      }

    val tasks: Seq[(String, Task[Unit])] =
      filesFromTo
        .map {
          case (from, to) => {
            val task =
              io.file.readAll[Task](Paths.get(from), 4096)
                .through(text.utf8Decode)
                .through(text.lines)
                .map(line => expand(substVariants, line))
                .intersperse("\n")
                .through(text.utf8Encode)
                .through {
                  ensureDirectoryExists(new File(to).getParent)
                  io.file.writeAll(Paths.get(to))
                }
                .run
            (to, task)
          }
        }
    (substVariants, tasks)
  }

  def splitPairs(s: String): Array[(String, String)] = {
    s.trim match {
      case "" => Array()
      case trimmed@_ =>
        trimmed.split(",")
          .map(fromTo(_))
    }
  }

  private def fromTo(s: String): (String, String) = {
    val trimmed = s.trim
    val terms = trimmed.split("=")
    if (terms.length != 2) {
      throw new RuntimeException(s"$s is not a valid term... must be of the form 'from=to'")
    }

    (terms(0), terms(1))
  }

  private val dirIgnoreSuffixes = Array("/.git", "/target", "/.idea", "/.gradle", "/build", "/classes")

  private val fileIgnoreSuffixes = Array("/.DS_Store", ".jar", ".class")

  private def recursiveFileObjects(dir: File): Seq[File] = {
    var result = mutable.Buffer[File]()

    val filesAndDirs = dir.listFiles
    if (filesAndDirs == null) {
      throw new RuntimeException(s"Invalid directory $dir")
    }

    for (file <- filesAndDirs) {
      val path = file.getCanonicalPath
      if (!file.isFile) {
        // Must be a directory - recurse
        if (!shouldIgnore(path, dirIgnoreSuffixes)) {
          result ++= recursiveFileObjects(file)
        }
      }
      else if (!shouldIgnore(path, fileIgnoreSuffixes)) {
        result += file
      }
    }

    result
  }

  def shouldIgnore(path: String, ignoreSuffixes: Array[String]): Boolean = {
    ignoreSuffixes
      .exists(path.endsWith(_))
  }

  /**
    * Apply all replacements to specified string
    */
  def expand(substs: Array[(String, String)], s: String) = {
    substs.foldLeft(s) {
      case (result, (from, to)) =>
        result.replaceAll(from, to)
    }
  }

  def variantsOf(substs: Array[(String, String)]) = {
    substs
      .flatMap {
        case (from, to) => {
          rawVariants(from, to)
        }
      }.distinct
  }

  def rawVariants(from: String, to: String) = {
    val lowerCase = (from.toLowerCase, to.toLowerCase)
    val upperCase = (from.toUpperCase, to.toUpperCase)
    val snakeCase = (Ascii.classToSnakeCase(from), Ascii.classToSnakeCase(to))
    val snakeUpperCase = (Ascii.classToSnakeUpperCase(from), Ascii.classToSnakeUpperCase(to))
    val snakeMinusCase = (Ascii.classToMinusSnakeCase(from), Ascii.classToMinusSnakeCase(to))
    val methodCase = (Ascii.classToMethodCase(from), Ascii.classToMethodCase(to))

    Array((from, to), lowerCase, upperCase, snakeCase, snakeUpperCase, snakeMinusCase, methodCase)
  }

  private def ensureDirectoryExists(dir: String): Unit = {
    val dirFile = new File(dir)
    if (!dirFile.exists) {
      val ok = dirFile.mkdirs
      if (!ok) {
        throw new RuntimeException(s"Could not create dir [$dir]")
      }

      dirFile.setWritable(true, false)
    }
  }
}
