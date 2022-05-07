package es.eriktorr.validate_hash
package infrastructure

import model.AccessDecision.Access
import model.AccessDecision.Access.{Forbidden, Granted}
import model.Password.{CipherText, ClearText, Format}
import model.{Password, Username}

import org.scalacheck.Gen

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
object Generators:
  def accessGen(password: Password[ClearText]): Gen[(Option[Password[CipherText]], Access)] =
    Gen.oneOf(
      forbiddenAccessGen(password),
      grantedAccessGen(password),
      unregisteredUserGen,
    )

  def passwordGen[A <: Format]: Gen[Password[A]] = textGen().map(Password.unsafeFrom[A])

  def usernameGen: Gen[Username] = textGen().map(Username.unsafeFrom)

  private[this] def textGen(minLength: Int = 3, maxLength: Int = 10): Gen[String] = for
    length <- Gen.choose(minLength, maxLength)
    text <- Gen.listOfN[Char](length, Gen.alphaNumChar).map(_.mkString)
  yield text

  private[this] def forbiddenAccessGen(
      password: Password[ClearText],
  ): Gen[(Option[Password[CipherText]], Access)] = for
    extra <- textGen()
    invalidPassword = password + extra
    hash <- Gen.some(Password.cipher(invalidPassword).toOption.get)
  yield (hash, Forbidden)

  private[this] def grantedAccessGen(
      password: Password[ClearText],
  ): Gen[(Option[Password[CipherText]], Access)] =
    Gen.some(Password.cipher(password).toOption.get).map((_, Granted))

  private[this] val unregisteredUserGen: Gen[(Option[Password[CipherText]], Access)] =
    Gen.const((Option.empty[Password[CipherText]], Forbidden))

  final case class User(
      username: Username,
      password: Password[ClearText],
      secret: Option[Password[CipherText]],
      access: Access,
  )
