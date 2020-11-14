package es.eriktorr.validate_hash.domain

import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.user._

object vault {
  trait Vault[F[_]] {
    def passwordFor(userName: UserName): F[Option[Password[CipherText]]]
  }
}
