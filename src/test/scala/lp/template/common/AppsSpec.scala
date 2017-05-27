package lp.template.common

import org.scalacheck.Prop._
import org.scalatest.FunSpec
import org.scalatest.prop.Checkers

class AppsSpec extends FunSpec with Checkers {
  describe("Common application behaviour") {
    it("should support exception details") {
      check {
        forAll {
          s: String =>
            val exception = new RuntimeException(s)
            val details = Apps.getExceptionDetails(exception)
            all(
              (details.contains(s) ?= true) :| "contains message",
              (details.contains(exception.getClass.getCanonicalName) ?= true) :| "contains exception class name"
            ) :| s"failing value was [$s]"
        }
      }
    }
  }
}
