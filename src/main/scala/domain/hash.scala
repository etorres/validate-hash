package es.eriktorr.validate_hash
package domain

import domain.error._
import domain.password.Password._
import domain.password._

import java.security.MessageDigest

object hash {
  val md5Hash: Password[ClearText] => Either[InvalidPassword, Password[CipherText]] =
    (password: Password[ClearText]) =>
      Password.fromString[CipherText](
        MessageDigest
          .getInstance("MD5")
          .digest(password.getBytes)
          .map(0xFF & _)
          .map("%02x".format(_))
          .foldLeft("")(_ + _)
      )
}
