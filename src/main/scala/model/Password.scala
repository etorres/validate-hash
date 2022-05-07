package es.eriktorr.validate_hash
package model

import AccessValidationError.InvalidPassword
import Password.Format

import org.tpolecat.typename.{typeName, TypeName}

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import scala.annotation.targetName
import scala.util.control.NoStackTrace

final case class Password[A <: Format] private (value: String):
  def getBytes: Array[Byte] = value.getBytes(StandardCharsets.UTF_8).nn
  @targetName("+")
  def +(value: String): Password[A] = Password(this.value + value)

object Password:
  sealed trait Format
  sealed trait ClearText extends Format
  sealed trait CipherText extends Format

  def unsafeFrom[A <: Format](value: String): Password[A] = Password(value)

  private[this] val base16Format = raw"([a-f\d]{32})".r

  def from[A <: Format](value: String)(implicit
      ev: TypeName[A],
  ): Either[InvalidPassword, Password[A]] = ev.value match
    case t if t == typeName[ClearText] =>
      if value.nonEmpty then Right(Password(value))
      else Left(InvalidPassword("Password cannot be empty"))
    case t if t == typeName[CipherText] =>
      if base16Format.matches(value) then Right(Password(value))
      else Left(InvalidPassword("Expected a base16 text of length 32"))
    case _ => Left(InvalidPassword("Unknown type"))

  private[this] val md5: MessageDigest = MessageDigest.getInstance("MD5").nn

  def cipher(password: Password[ClearText]): Either[InvalidPassword, Password[CipherText]] =
    import scala.language.unsafeNulls
    Password.from[CipherText](
      md5
        .digest(password.getBytes)
        .map(0xff & _)
        .map("%02x".format(_))
        .foldLeft("")(_ + _),
    )
