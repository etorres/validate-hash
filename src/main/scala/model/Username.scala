package es.eriktorr.validate_hash
package model

import model.AccessValidationError.InvalidUsername

opaque type Username = String

object Username:
  def unsafeFrom(value: String): Username = value

  def from(value: String): Either[InvalidUsername, Username] = if value.nonEmpty then
    Right(unsafeFrom(value))
  else Left(InvalidUsername("Username cannot be empty"))

  extension (username: Username) def value: String = username
