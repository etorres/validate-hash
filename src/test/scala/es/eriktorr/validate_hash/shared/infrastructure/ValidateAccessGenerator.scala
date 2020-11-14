package es.eriktorr.validate_hash.shared.infrastructure

import es.eriktorr.validate_hash.domain.error._
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.user._
import es.eriktorr.validate_hash.shared.infrastructure.GenericGenerators.nonBlankStringOfAtMost
import es.eriktorr.validate_hash.shared.infrastructure.NameGenerator.distinctNameGen
import org.scalacheck._

object ValidateAccessGenerator {
  type UserNameWithPassword = (UserName, (Password[ClearText], Password[CipherText]))

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def distinctUserNameGen(n: Int): Gen[List[UserName]] =
    distinctNameGen(n).map(_.map(UserName.fromString(_).toOption.get))

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
