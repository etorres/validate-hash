package es.eriktorr.validate_hash
package application

import domain.access.Access._
import domain.access._
import domain.error.InvalidPassword
import domain.password.Password._
import domain.password._
import domain.user._
import domain.vault._

import cats.effect._
import cats.implicits._

trait ValidateAccess[F[_]] {
  def grantAccessIdentifiedWith(
    userName: UserName,
    password: Password[ClearText]
  ): F[AccessDecision]
}

object ValidateAccess {
  def impl[F[_]: Sync](
    vault: Vault[F],
    hashFunction: Password[ClearText] => Either[InvalidPassword, Password[CipherText]]
  ): ValidateAccess[F] =
    (userName: UserName, password: Password[ClearText]) => {
      val F = Sync[F]
      for {
        cipherText <- F.fromEither(hashFunction(password))
        maybeHash <- vault.passwordFor(userName)
        access = maybeHash match {
          case Some(expectedHash) => if (expectedHash === cipherText) Granted else Forbidden
          case None => Forbidden
        }
      } yield AccessDecision(userName, access)
    }

  def impl2[F[_]: Sync](
    vault: Vault[F],
    hashFunction: Password[ClearText] => Either[InvalidPassword, Password[CipherText]]
  ): ValidateAccess[F] = {
    def validated(password: Password[ClearText])(f: Password[CipherText] => F[Access]) = {
      val F = Sync[F]
      F.fromEither(hashFunction(password)).flatMap(f)
    }

    (userName: UserName, password: Password[ClearText]) =>
      validated(password) { actualHash =>
        vault
          .passwordFor(userName)
          .map {
            case Some(expectedHash) => if (expectedHash === actualHash) Granted else Forbidden
            case None => Forbidden
          }
      }.map(AccessDecision(userName, _))
  }
}
