package es.eriktorr.validate_hash.shared.infrastructure

import es.eriktorr.validate_hash.domain.error._
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.user._
import es.eriktorr.validate_hash.shared.infrastructure.PasswordGenerator.passwordGen
import es.eriktorr.validate_hash.shared.infrastructure.UserNameGenerator.userNameGen
import org.scalacheck._

object ValidateAccessGenerator {
  type UserNameWithPassword = (UserName, (Password[ClearText], Password[CipherText]))

  def userNameWithPassword(
    hashFunction: Password[ClearText] => Either[InvalidPassword, Password[CipherText]]
  ): Gen[UserNameWithPassword] =
    for {
      userName <- userNameGen
      (password, passwordHash) <- passwordGen(hashFunction)
    } yield (userName, (password, passwordHash))
}
