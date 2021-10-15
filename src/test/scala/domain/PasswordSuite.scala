package es.eriktorr.validate_hash
package domain

import domain.password.Password._
import domain.password._

import cats.effect._
import cats.implicits._
import weaver.SimpleIOSuite

object PasswordSuite extends SimpleIOSuite {

  test("Compute password hash from text") {
    for {
      (password, expected) <- (
        IO.fromEither(Password.fromString[ClearText]("secret")),
        IO.fromEither(Password.fromString[CipherText]("5ebe2294ecd0e0f08eab7690d2a6ee69"))
      ).tupled
      hash <- IO.fromEither(Password.encrypted(password))
    } yield expect(hash === expected)
  }

}
