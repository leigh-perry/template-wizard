package lp.template.testsupport

import lp.template.testsupport.TestProperties.genPicksAlpha
import org.scalacheck.Prop._
import org.scalatest.FunSpec
import org.scalatest.prop.Checkers

class TestPropertiesSpec extends FunSpec with Checkers {
  describe("Properties for matching / non-matching picks") {
    it("should generate picks that exclude the invalid entries") {
      check {
        forAll(genPicksAlpha) {
          case (validKeysAndValues, invalidPicks) =>
            invalidPicks.forall(!validKeysAndValues.contains(_))
        }
      }
    }

    it("should generate invalid entries that exclude the picks") {
      check {
        forAll(genPicksAlpha) {
          case (validKeysAndValues, invalidPicks) =>
            validKeysAndValues.keySet.forall(!invalidPicks.contains(_))
        }
      }
    }
  }
}
