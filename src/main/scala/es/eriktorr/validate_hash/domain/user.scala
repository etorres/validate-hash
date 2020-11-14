package es.eriktorr.validate_hash.domain

import cats._
import cats.implicits._
import es.eriktorr.validate_hash.domain.error._
import eu.timepit.refined._
import eu.timepit.refined.predicates.all._
import eu.timepit.refined.types.all._
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

object user {
  @newtype class UserName(val unUserName: NonEmptyString)

  object UserName {
    def fromString(str: String): Either[InvalidUserName, UserName] =
      refineV[NonEmpty](str) match {
        case Left(_) => InvalidUserName("User name cannot be empty").asLeft
        case Right(refinedStr) => refinedStr.coerce[UserName].asRight
      }

    implicit val eqUserName: Eq[UserName] = Eq.fromUniversalEquals
    implicit val showUserName: Show[UserName] = Show.show(_.toString)
  }
}
