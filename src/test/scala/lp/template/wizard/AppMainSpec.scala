package lp.template.wizard

import lp.template.testsupport.TestProperties
import lp.template.testsupport.TestProperties._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalatest.FunSpec
import org.scalatest.prop.Checkers

class AppMainSpec extends FunSpec with Checkers {
  describe("File scanning") {
    it("should ignore specific directories and files by matching suffix") {
      check {
        forAll(
          for {
            prefix <- genNonEmptyAlpha
            validPicks <- listOf(TestProperties.genNonEmptyAlpha)
          } yield (prefix, validPicks)
        ) {
          case (prefix: String, ignorables: Seq[String]) =>
            ignorables.forall {
              validPick =>
                AppMain.shouldIgnore(s"$prefix/$validPick", ignorables.toArray)
            }
        }
      }
    }

    it("should not ignore specific directories and files that don't match suffix") {
      // TODO express this
      //      check {
      //        forAll(
      //          for {
      //            prefix <- genNonEmptyAlpha
      //            (mapValidPicks, invalidPicks) <- genPicksAlpha
      //          } yield (prefix, mapValidPicks, invalidPicks)
      //        ) {
      //          case (prefix, mapValidPicks, nonignorables) =>
      //            val ignorables = mapValidPicks.keys.toArray
      //
      //            nonignorables.forall {
      //              suffix =>
      //                !AppMain.shouldIgnore(s"$prefix/$suffix", ignorables)
      //            }
      //        }
      //      }
    }
  }

  describe("String variants") {
    it("should include full set for multiple terms") {
      check {
        forAll(genTwoTermWord :| "from", genTwoTermWord :| "to") {
          (from, to) =>
            val variants = AppMain.variantsOf(Array((from, to)))
            variants.deep ?= AppMain.rawVariants(from, to).deep
        }
      }
    }

    it("should include reduced set for single terms of two or more chars") {
      check {
        forAll(genWord.suchThat(_.length > 1) :| "term 1", genWord.suchThat(_.length > 1) :| "term 2") {
          (from, to) =>
            val variants = AppMain.variantsOf(Array((from, to)))
            val lowerCase = (from.toLowerCase, to.toLowerCase)
            val upperCase = (from.toUpperCase, to.toUpperCase)
            val expected = Array((from, to), lowerCase, upperCase)
            variants.deep ?= expected.deep
        }
      }
    }

    it("should include reduced set for single terms of 1 char") {
      check {
        forAll(alphaUpperChar.map(_.toString), alphaUpperChar.map(_.toString)) {
          (from, to) =>
            val variants = AppMain.variantsOf(Array((from, to)))
            val lowerCase = (from.toLowerCase, to.toLowerCase)
            val expected = Array((from, to), lowerCase)
            variants.deep ?= expected.deep
        }
      }
    }
  }

  describe("String expansion") {
    it("should handle case of no substitutions") {
      check {
        forAll(alphaStr) {
          s =>
            s =? AppMain.expand(Array(), s)
        }
      }
    }

    it("should handle case of a single substitution") {
      val input = "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass"
      val expected = "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing"
      val substitutions = Array(("SomeClass", "AnoTherThing"))
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
        val substitutions = Array(("SomeClass", "AnoTherThing"), ("AThirdElement", "SecondExpected"))
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
        val substitutions = Array(("SomeClass", "AnoTherThing"), ("AThirdElement", "SecondExpected"), ("FthE", "FhQ"))
        AppMain.expand(AppMain.variantsOf(substitutions), input)
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
              (pairs.length ?= 12) :| "pair length",
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
}
