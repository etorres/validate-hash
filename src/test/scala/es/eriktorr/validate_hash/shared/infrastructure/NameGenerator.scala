package es.eriktorr.validate_hash.shared.infrastructure

import org.scalacheck._

import scala.util.Random

object NameGenerator {
  private[this] val names = List(
    "Olivia",
    "Oliver",
    "Amelia",
    "George",
    "Maya",
    "Noah",
    "Ava",
    "Arthur",
    "Mia",
    "Harry",
    "Isabella",
    "Leo",
    "Sophia",
    "Muhammad",
    "Grace",
    "Jack",
    "Lily",
    "Charlie",
    "Freya",
    "Oscar",
    "Emily",
    "Jacob",
    "Ivy",
    "Henry",
    "Ella",
    "Thomas",
    "Rosie",
    "Freddie",
    "Evie",
    "Alfie",
    "Theo",
    "Florence",
    "William",
    "Poppy",
    "Theodore",
    "Charlotte",
    "Archie",
    "Willow",
    "Joshua",
    "Evelyn"
  )

  def distinctNameGen(n: Int): Gen[List[String]] = Gen.const(Random.shuffle(names).take(n))
}
