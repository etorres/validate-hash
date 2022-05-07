package es.eriktorr.validate_hash
package unit

import model.Password
import model.Password.{CipherText, ClearText}

import munit.FunSuite

final class PasswordUnitTest extends FunSuite:

  test("it should cipher a password") {
    for
      password <- Password.from[ClearText]("secret")
      expected <- Password.from[CipherText]("5ebe2294ecd0e0f08eab7690d2a6ee69")
      hash <- Password.cipher(password)
    yield assertEquals(hash, expected)
  }
