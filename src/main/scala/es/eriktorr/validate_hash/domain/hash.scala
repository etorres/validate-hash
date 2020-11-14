package es.eriktorr.validate_hash.domain

import java.security.MessageDigest

import es.eriktorr.validate_hash.domain.error._
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.password._

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
