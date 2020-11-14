package es.eriktorr.validate_hash.shared.infrastructure

import org.scalacheck._

object GenericGenerators {
  def stringOfAtMost(maxLength: Int, charGen: Gen[Char]): Gen[String] =
    for {
      length <- Gen.choose(1, maxLength)
      string <- Gen.listOfN(length, charGen).map(_.mkString)
    } yield string

  def nonBlankStringOfAtMost(maxLength: Int): Gen[String] =
    stringOfAtMost(maxLength, Gen.alphaNumChar)
}
