package es.eriktorr.validate_hash
package shared.infrastructure

import domain.error.InvalidPassword
import domain.password.Password
import domain.password.Password._
import domain.user.UserName
import shared.infrastructure.GenericGenerators.nonBlankStringOfAtMost
import shared.infrastructure.NameGenerator.distinctNameGen

import cats.data.Nested
import cats.implicits._
import org.scalacheck.Gen
import org.scalacheck.cats.implicits._

object ValidateAccessGenerator {
  type UserNameWithPassword = (UserName, (Password[ClearText], Password[CipherText]))

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def distinctUserNameGen(n: Int): Gen[List[UserName]] =
    Nested(distinctNameGen(n)).map(UserName.fromString(_).toOption.get).value

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def passwordGen(
    hashFunction: Password[ClearText] => Either[InvalidPassword, Password[CipherText]]
  ): Gen[(Password[ClearText], Password[CipherText])] =
    for {
      password <- nonBlankStringOfAtMost(8).map(
        Password.fromString[ClearText](_).toOption.get
      )
      hash = hashFunction(password).toOption.get
    } yield (password, hash)

  def userNameWithPasswordGen(
    userNameGen: Gen[UserName],
    passwordGen: Gen[(Password[ClearText], Password[CipherText])]
  ): Gen[(UserName, (Password[ClearText], Password[CipherText]))] =
    for {
      userName <- userNameGen
      (password, passwordHash) <- passwordGen
    } yield (userName, (password, passwordHash))

}
