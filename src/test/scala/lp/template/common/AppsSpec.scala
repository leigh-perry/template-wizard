package lp.template.common

import org.scalacheck.Prop.forAll
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
            details.contains(s) && details.contains(exception.getClass.getCanonicalName)
        }
      }
    }
  }
}
