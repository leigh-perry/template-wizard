package lp.template.wizard

import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalatest.FunSpec
import org.scalatest.prop.Checkers

class AsciiSpec extends FunSpec with Checkers {
  /**
    * @return eg (Someword, someword)
    */
  def genWord: Gen[(String, String)] = {
    for {
      c <- alphaUpperChar
      cs <- resize(4, listOf(alphaLowerStr))
      s = (c :: cs).mkString
    } yield (s, s.toLowerCase)
  }

  describe("ASCII conversion") {
    it("should handle JavaClass 1+ char words to snake_case") {
      assertResult("a_cd_ef")(Ascii.classToSnakeCase("ACdEf"))
      assertResult("ac_d_ef")(Ascii.classToSnakeCase("AcDEf"))
      assertResult("ab_cd_ef")(Ascii.classToSnakeCase("AbCdEF"))
      assertResult("a_cd_ef")(Ascii.classToSnakeCase("ACdEF"))
      assertResult("ac_def")(Ascii.classToSnakeCase("AcDEF"))
      assertResult("ab_cdef")(Ascii.classToSnakeCase("AbCDEF"))
    }

    it("should handle JavaClass 2+ char words to snake_case") {
      assertResult("a_cd_ef")(Ascii.classToSnakeCase("ACdEf"))
      assertResult("ab_cd_ef")(Ascii.classToSnakeCase("AbCdEf"))
      assertResult("ab_c_ef")(Ascii.classToSnakeCase("AbCEf"))
      assertResult("qcykag_xnbyu")(Ascii.classToSnakeCase("QcykagXNBYU"))
      assertResult("qcykagxnbyu")(Ascii.classToSnakeCase("QCYKAGXNBYU"))

      check {
        forAll {
          for {
            w <- nonEmptyListOf(genWord)
            c <- alphaLowerChar
          } yield (w.map(_._1 + c).mkString, w.map(_._2 + c).mkString("_"))
        } {
          case (classCase, snakeCase) =>
            Ascii.classToSnakeCase(classCase) == snakeCase
        }
      }
    }

    it("should handle JavaClass 2+ char words to minus-snake-case") {
      check {
        forAll {
          for {
            w <- nonEmptyListOf(genWord)
            c <- alphaLowerChar
          } yield (w.map(_._1 + c).mkString, w.map(_._2 + c).mkString("-"))
        } {
          case (classCase, snakeCase) =>
            Ascii.classToMinusSnakeCase(classCase) == snakeCase
        }
      }
    }

    it("should handle JavaClass 2+ char words to UPPER_SNAKE_CASE") {
      check {
        forAll {
          for {
            w <- nonEmptyListOf(genWord)
            c <- alphaLowerChar
          } yield (w.map(_._1 + c).mkString, w.map(_._2 + c).mkString("_").toUpperCase())
        } {
          case (classCase, snakeCase) =>
            Ascii.classToSnakeUpperCase(classCase) == snakeCase
        }
      }
    }

    it("should handle JavaClass 2+ char words to methodCase") {
      check {
        forAll {
          for {
            w <- nonEmptyListOf(genWord)
            c <- alphaLowerChar
          } yield w.map(_._1 + c).mkString
        } {
          classCase =>
            val method = Ascii.classToMethodCase(classCase)
            method.charAt(0) == classCase.charAt(0).toLower &&
              classCase.substring(1) == method.substring(1)
        }
      }
    }
  }
}
