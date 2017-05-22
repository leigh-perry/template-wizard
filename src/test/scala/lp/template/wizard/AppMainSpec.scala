package lp.template.wizard

import lp.template.testsupport.TestProperties
import lp.template.testsupport.TestProperties._
import org.scalacheck.Arbitrary._
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
            (mapValidPicks, invalidPicks) <- genPicks[String, String]
          } yield (prefix, mapValidPicks, invalidPicks)
        ) {
          case (prefix, mapValidPicks, invalidPicks) =>
            val validPicks = mapValidPicks.keys.toArray

            validPicks.forall(validPick => AppMain.shouldIgnore(s"$prefix/$validPick", validPicks)) &&
              invalidPicks.forall(invalidPick => !AppMain.shouldIgnore(s"$prefix/$invalidPick", validPicks))
        }
      }
    }
  }

  describe("String expansion") {
    it("should handle case of no substitutions") {
      check {
        forAll(alphaStr) {
          s =>
            s == AppMain.expand(Array(), s)
        }
      }
    }

    it("should handle case of a single substitution") {
      val input = "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass"
      val expected = "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing"
      assertResult(expected)(AppMain.expand(Array(("SomeClass", "AnoTherThing")), input))
    }

    it("should handle case of two substitutions") {
      val input = "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass " +
        "a AThirdElement athirdelement ATHIRDELEMENT a_third_element a-third-element A_THIRD_ELEMENT aThirdElement"
      val expected = "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing " +
        "a SecondExpected secondexpected SECONDEXPECTED second_expected second-expected SECOND_EXPECTED secondExpected"

      assertResult(expected) {
        AppMain.expand(Array(("SomeClass", "AnoTherThing"), ("AThirdElement", "SecondExpected")), input)
      }
    }

    it("should handle case of three substitutions") {
      val input = "a SomeClass someclass SOMECLASS some_class some-class SOME_CLASS someClass " +
        "a AThirdElement athirdelement ATHIRDELEMENT a_third_element a-third-element A_THIRD_ELEMENT aThirdElement" +
        "a FthE fthe FTHE fth_e fth-e FTH_E fthE"
      val expected = "a AnoTherThing anotherthing ANOTHERTHING ano_ther_thing ano-ther-thing ANO_THER_THING anoTherThing " +
        "a SecondExpected secondexpected SECONDEXPECTED second_expected second-expected SECOND_EXPECTED secondExpected" +
        "a FhQ fhq FHQ fh_q fh-q FH_Q fhQ"

      assertResult(expected) {
        AppMain.expand(
          Array(("SomeClass", "AnoTherThing"), ("AThirdElement", "SecondExpected"), ("FthE", "FhQ")),
          input
        )
      }
    }
  }

  describe("Substitution argument handling") {
    it("should handle empty string") {
      check {
        forAll(singleCondition) {
          x =>
            val pairs: Array[(String, String)] = AppMain.splitPairs("")
            pairs.length == 0
        }
      }
    }

    it("should handle a single substitution") {
      check {
        forAll(genNonEmptyAlphaPair) {
          case (from, to) =>
            val pairs = AppMain.splitPairs(s"$from=$to")
            pairs.length == 1 && pairs(0) == (from, to)
        }
      }
    }

    it("should handle multiple substitutions separated by comma") {
      check {
        forAll(genTwoOrMoreStringPairs) {
          inputPairs: List[(String, String)] =>
            val s = asString(inputPairs)
            inputPairs == AppMain.splitPairs(s).toList
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
