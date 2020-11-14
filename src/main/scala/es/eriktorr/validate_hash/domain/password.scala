package es.eriktorr.validate_hash.domain

import cats._
import cats.implicits._
import es.eriktorr.validate_hash.domain.error._
import eu.timepit.refined._
import eu.timepit.refined.predicates.all._
import eu.timepit.refined.types.all._
import io.estatico.newtype._
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

object password {
  @newtype class Password[A <: Password.Format](val unPassword: NonEmptyString) {
    def getBytes: Array[Byte] = unPassword.value.getBytes()
  }

  object Password {
    sealed trait Format
    sealed trait ClearText extends Format
    sealed trait CipherText extends Format

    // This is only needed for IntelliJ to compile
    implicit def ev[A <: Password.Format, B]: Coercible[B, Password[A]] =
      Coercible.instance[B, Password[A]]

    def fromString[A <: Password.Format](str: String): Either[InvalidPassword, Password[A]] =
      refineV[NonEmpty](str) match {
        case Left(_) => InvalidPassword("Password cannot be empty").asLeft
        case Right(refinedStr) => refinedStr.coerce[Password[A]].asRight
      }

    implicit def eqPassword[A <: Password.Format]: Eq[Password[A]] = Eq.fromUniversalEquals
    implicit def showPassword[A <: Password.Format]: Show[Password[A]] = Show.show(_.toString)
  }
}
