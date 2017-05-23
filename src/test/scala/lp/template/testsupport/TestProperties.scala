package lp.template.testsupport

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen._
import org.scalatest.FunSpec
import org.scalatest.prop.Checkers

object TestProperties extends FunSpec with Checkers {
  def singleCondition: Gen[Int] = {
    choose(0, 0)
  }

  def genNonEmptyAlpha = {
    alphaStr.filter(_.length > 0)
  }

  def genNonEmptyAlphaPair: Gen[(String, String)] = {
    for {
      from <- "from" |: genNonEmptyAlpha
      to <- "to" |: genNonEmptyAlpha
    } yield (from, to)
  }

  def genTwoOrMore: Gen[Int] = {
    choose(2, 10)
  }

  def genTwoOrMoreStringPairs: Gen[List[(String, String)]] = {
    for {
      count <- "count" |: genTwoOrMore
      tuples <- listOfN(count, genNonEmptyAlphaPair)
    } yield tuples
  }

  def genPicks[A, B](implicit aa: Arbitrary[A], ab: Arbitrary[B]): Gen[(Map[A, B], List[A])] = {
    for {
      pairs <- arbitrary[Map[A, B]]
      validKeys = pairs.keySet
      anotherList <- listOf(arbitrary[A])
      invalidPicks = anotherList.filterNot(i => validKeys.contains(i))
    } yield (pairs, invalidPicks)
  }

  def genPicksAlpha: Gen[(Map[String, String], List[String])] = {
    for {
      pairs <- nonEmptyMap[String, String](genNonEmptyAlphaPair)
      validKeys = pairs.keySet
      anotherList <- listOf(genNonEmptyAlpha)
      invalidPicks = anotherList.filterNot(i => validKeys.contains(i))
    } yield (pairs, invalidPicks)
  }
}
