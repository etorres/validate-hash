package es.eriktorr.validate_hash
package domain

import domain.password.Password._
import domain.password._
import domain.user._

object vault {
  trait Vault[F[_]] {
    def passwordFor(userName: UserName): F[Option[Password[CipherText]]]
  }
}
