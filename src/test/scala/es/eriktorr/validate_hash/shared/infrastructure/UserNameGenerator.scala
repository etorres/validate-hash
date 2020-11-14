package es.eriktorr.validate_hash.shared.infrastructure

import es.eriktorr.validate_hash.domain.user._
import org.scalacheck._

object UserNameGenerator {
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

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  val userNameGen: Gen[UserName] = Gen.oneOf(names).map(UserName.fromString(_).toOption.get)
}
