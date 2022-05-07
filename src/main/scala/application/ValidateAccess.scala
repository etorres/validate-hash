package es.eriktorr.validate_hash
package application

import model.AccessDecision.Access.{Forbidden, Granted}
import model.AccessValidationError.InvalidPassword
import model.Password.{CipherText, ClearText}
import model.{AccessDecision, Password, Username, Vault}

import cats.effect.IO

trait ValidateAccess:
  def grantAccessIdentifiedBy(username: Username, password: Password[ClearText]): IO[AccessDecision]

object ValidateAccess:
  def impl(
      vault: Vault,
      hashFunction: Password[ClearText] => Either[InvalidPassword, Password[CipherText]],
  ): ValidateAccess = (username: Username, password: Password[ClearText]) =>
    for
      cipherText <- IO.fromEither(hashFunction(password))
      maybeHash <- vault.passwordFor(username)
      access = maybeHash match
        case Some(expectedHash) => if expectedHash == cipherText then Granted else Forbidden
        case None => Forbidden
    yield AccessDecision(username, access)
