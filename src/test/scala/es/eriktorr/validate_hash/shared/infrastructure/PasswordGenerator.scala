package es.eriktorr.validate_hash.shared.infrastructure

import es.eriktorr.validate_hash.domain.error._
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.password.Password._
import org.scalacheck._

object PasswordGenerator {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def passwordGen(
    hashFunction: Password[ClearText] => Either[InvalidPassword, Password[CipherText]]
  ): Gen[(Password[ClearText], Password[CipherText])] =
    for {
      password <- Gen.alphaNumStr.map(Password.fromString[ClearText](_).toOption.get)
      hash = hashFunction(password).toOption.get
    } yield (password, hash)
}
