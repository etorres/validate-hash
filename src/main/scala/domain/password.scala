package es.eriktorr.validate_hash
package domain

import domain.error._

import cats._
import cats.implicits._
import eu.timepit.refined._
import eu.timepit.refined.predicates.all._
import eu.timepit.refined.types.all._
import io.estatico.newtype._
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import org.tpolecat.typename.{typeName, TypeName}

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import scala.reflect.runtime.universe.{typeOf, TypeTag}

object password {
  @newtype class Password[A <: Password.Format](val unPassword: NonEmptyString) {
    def getBytes: Array[Byte] = unPassword.value.getBytes(StandardCharsets.UTF_8)
  }

  object Password {
    sealed trait Format
    sealed trait ClearText extends Format
    sealed trait CipherText extends Format

    // This is only needed for IntelliJ to compile
    implicit def ev[A <: Password.Format, B]: Coercible[B, Password[A]] =
      Coercible.instance[B, Password[A]]

    def fromString[A <: Password.Format](
      str: String
    )(implicit ev: TypeName[A]): Either[InvalidPassword, Password[A]] = ev.value match {
      case t if t == typeName[ClearText] =>
        refineV[NonEmpty](str) match {
          case Left(_) => InvalidPassword("Password cannot be empty").asLeft
          case Right(refinedStr) => refinedStr.coerce[Password[A]].asRight
        }
      case t if t == typeName[CipherText] =>
        if (!"^([a-f0-9]{32})$".r.matches(str))
          InvalidPassword("Expected a base16 text of length 32").asLeft
        else str.coerce[Password[A]].asRight
      case _ => InvalidPassword("Unknown type").asLeft
    }

    def fromStringWithReflection[A <: Password.Format](
      str: String
    )(implicit tag: TypeTag[A]): Either[InvalidPassword, Password[A]] = tag.tpe match {
      case t if t =:= typeOf[ClearText] =>
        refineV[NonEmpty](str) match {
          case Left(_) => InvalidPassword("Password cannot be empty").asLeft
          case Right(refinedStr) => refinedStr.coerce[Password[A]].asRight
        }
      case t if t =:= typeOf[CipherText] =>
        if (!"^([a-f0-9]{32})$".r.matches(str))
          InvalidPassword("Expected a base16 text of length 32").asLeft
        else str.coerce[Password[A]].asRight
      case _ => InvalidPassword("Unknown type").asLeft
    }

    def encrypted(password: Password[ClearText]): Either[InvalidPassword, Password[CipherText]] =
      Password.fromString[CipherText](
        MessageDigest
          .getInstance("MD5")
          .digest(password.getBytes)
          .map(0xFF & _)
          .map("%02x".format(_))
          .foldLeft("")(_ + _)
      )

    implicit def eqPassword[A <: Password.Format]: Eq[Password[A]] = Eq.fromUniversalEquals
    implicit def showPassword[A <: Password.Format]: Show[Password[A]] = Show.show(_.toString)
  }
}
