package lp.template.testsupport

import org.scalacheck.Gen
import org.scalacheck.Gen._

object TestProperties {

  object ArbitraryImplicits {
    //implicit val arbNonEmptyAlphaPair: Arbitrary[(String, String)] = Arbitrary(genNonEmptyAlphaPair)
  }

  private def genAlphaLowerStrN(size: Int) = {
    resize(size, listOf(alphaLowerStr))
  }

  def genTerm: Gen[String] = {
    for {
      c <- alphaUpperChar
      cs <- genAlphaLowerStrN(10)
    } yield s"$c${cs.mkString}"
  }

  def genTwoTerm: Gen[String] = {
    for {
      w0 <- genTerm
      w1 <- genTerm
    } yield s"$w0$w1"
  }

  def genNonEmptyAlpha = {
    alphaStr.suchThat(_.length > 0)
  }

  def genNonEmptyAlphaPair: Gen[(String, String)] = {
    for {
      from <- genNonEmptyAlpha :| "from"
      to <- genNonEmptyAlpha :| "to"
    } yield (from, to)
  }

  def genTwoOrMoreStringPairs: Gen[List[(String, String)]] = {
    for {
      count <- choose(2, 10) :| "count"
      tuples <- listOfN(count, genNonEmptyAlphaPair)
    } yield tuples
  }

  //  def genPicks[A, B](implicit aa: Arbitrary[A], ab: Arbitrary[B]): Gen[(Map[A, B], List[A])] = {
  //    for {
  //      pairs <- arbitrary[Map[A, B]]
  //      validKeys = pairs.keySet
  //      anotherList <- listOf(arbitrary[A])
  //      invalidPicks = anotherList.filterNot((i: A) => validKeys.contains(i))
  //    } yield (pairs, invalidPicks)
  //  }

  def genPicksAlpha: Gen[(Map[String, String], List[String])] = {
    for {
      validKeyValues <- nonEmptyMap[String, String](genNonEmptyAlphaPair)
      validKeys = validKeyValues.keySet
      invalidPicks <- listOf(genNonEmptyAlpha.suchThat(!validKeys.contains(_)))
    } yield (validKeyValues, invalidPicks)
  }
}
