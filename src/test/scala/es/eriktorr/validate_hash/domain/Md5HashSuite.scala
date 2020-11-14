package es.eriktorr.validate_hash.domain

import cats.effect._
import cats.implicits._
import es.eriktorr.validate_hash.domain.hash.md5Hash
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.password.Password._
import weaver.SimpleIOSuite

object Md5HashSuite extends SimpleIOSuite {
  simpleTest("Compute MD5 hash from text") {
    for {
      (password, expected) <- (
        IO.fromEither(Password.fromString[ClearText]("secret")),
        IO.fromEither(Password.fromString[CipherText]("5ebe2294ecd0e0f08eab7690d2a6ee69"))
      ).tupled
      hash <- IO.fromEither(md5Hash(password))
    } yield expect(hash == expected)
  }
}
