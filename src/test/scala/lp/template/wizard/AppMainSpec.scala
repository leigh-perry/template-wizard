package lp.template.wizard

import lp.template.testsupport.TestProperties._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalatest.FunSpec
import org.scalatest.prop.Checkers

class AppMainSpec extends FunSpec with Checkers {
  describe("File scanning") {
    it("should ignore by matching suffix") {
      check {
        forAll(
          for {
            prefix <- genNonEmptyAlpha
            ignorables <- listOf(genNonEmptyAlpha)
          } yield (prefix, ignorables)
        ) {
          case (prefix: String, ignorables: Seq[String]) =>
            ignorables.forall {
              suffix =>
                AppMain.shouldIgnore(s"$prefix/$suffix", ignorableSuffixesOf(ignorables))
            }
        }
      }
    }

    it("should not ignore non-matching suffix") {
      def picks(prefix: String) = {
        genPicksAlpha.suchThat {
          case (_, ignorables) => !ignorables.contains(prefix)
        }
      }

      check {
        forAll(
          for {
            prefix <- genNonEmptyAlpha
            (validPicks, invalidPicks) <- picks(prefix)
          } yield (prefix, validPicks, invalidPicks)
        ) {
          case (prefix, nonIgnorables: Map[String, String], ignorables: Seq[String]) =>
            // "prefix/suffix"
            nonIgnorables.keySet.forall {
              suffix =>
                !AppMain.shouldIgnore(s"$prefix/$suffix", ignorableSuffixesOf(ignorables))
            }
        }
      }
    }
  }

  describe("String expansion") {
    it("should handle case of no substitutions") {
      check {
        forAll(alphaStr) {
          s =>
            s =? AppMain.expand(Vector(), s)
        }
      }
    }

    it("should handle case of a single substitution") {
      val input = "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass"
      val expected = "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing"
      val substitutions = Vector(("SomeClass", "AnoTherThing"))
      assertResult(expected)(AppMain.expand(AppMain.variantsOf(substitutions), input))
    }

    it("should handle case of two substitutions") {
      val input =
        "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass " +
          "a AThirdElement athirdelement ATHIRDELEMENT a_third_element a-third-element A_THIRD_ELEMENT aThirdElement"
      val expected =
        "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing " +
          "a SecondExpected secondexpected SECONDEXPECTED second_expected second-expected SECOND_EXPECTED secondExpected"

      assertResult(expected) {
        val substitutions = Vector(("SomeClass", "AnoTherThing"), ("AThirdElement", "SecondExpected"))
        AppMain.expand(AppMain.variantsOf(substitutions), input)
      }
    }

    it("should handle case of three substitutions") {
      val input =
        "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass " +
          "a AThirdElement athirdelement ATHIRDELEMENT a_third_element a-third-element A_THIRD_ELEMENT aThirdElement" +
          "a FthE fthe FTHE fth_e fth-e FTH_E fthE"
      val expected =
        "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing " +
          "a SecondExpected secondexpected SECONDEXPECTED second_expected second-expected SECOND_EXPECTED secondExpected" +
          "a FhQ fhq FHQ fh_q fh-q FH_Q fhQ"

      assertResult(expected) {
        val substitutions = Vector(("SomeClass", "AnoTherThing"), ("AThirdElement", "SecondExpected"), ("FthE", "FhQ"))
        AppMain.expand(AppMain.variantsOf(substitutions), input)
      }
    }
  }

  describe("String variants") {
    it("should include full set for multiple terms") {
      check {
        forAll(genTwoTerm :| "from", genTwoTerm :| "to") {
          (from, to) =>
            val variants = AppMain.variantsOf(Vector((from, to)))
            variants ?= AppMain.rawVariants(from, to)
        }
      }
    }

    it("should include reduced set for single terms of two or more chars") {
      check {
        forAll(genTerm.suchThat(_.length > 1) :| "term 1", genTerm.suchThat(_.length > 1) :| "term 2") {
          (from, to) =>
            val variants = AppMain.variantsOf(Vector((from, to)))
            val lowerCase = (from.toLowerCase, to.toLowerCase)
            val upperCase = (from.toUpperCase, to.toUpperCase)
            val expected = Vector((from, to), lowerCase, upperCase)
            variants ?= expected
        }
      }
    }

    it("should include reduced set for single terms of 1 char") {
      check {
        forAll(alphaUpperChar.map(_.toString), alphaUpperChar.map(_.toString)) {
          (from, to) =>
            val variants = AppMain.variantsOf(Vector((from, to)))
            val lowerCase = (from.toLowerCase, to.toLowerCase)
            val expected = Vector((from, to), lowerCase)
            variants ?= expected
        }
      }
    }
  }

  describe("Substitution argument handling") {
    it("should handle empty string") {
      assertResult(0)(AppMain.splitPairs("").length)
    }

    it("should handle a single substitution") {
      check {
        forAll(genNonEmptyAlphaPair) {
          case (from, to) =>
            val pairs = AppMain.splitPairs(s"$from=$to")
            all(
              (pairs.length ?= 1) :| "pair length",
              (pairs(0) ?= (from, to)) :| "pair contents"
            )
        }
      }
    }

    it("should handle multiple substitutions separated by comma") {
      check {
        forAll(genTwoOrMoreStringPairs) {
          inputPairs: List[(String, String)] =>
            val s = asString(inputPairs)
            inputPairs =? AppMain.splitPairs(s).toList
        }
      }
    }
  }

  private[this] def asString(inputPairs: List[(String, String)]) = {
    inputPairs
      .map {
        case (from, to) => s"$from=$to"
      }
      .mkString(",")
  }

  private[this] def ignorableSuffixesOf(ignorables: List[String]) = {
    ignorables
      .map(s => s"/$s")
      .toVector
  }
}
