package es.eriktorr.validate_hash
package domain

import scala.util.control.NoStackTrace

object error {
  sealed trait ValidateHashError extends NoStackTrace with Product with Serializable

  final case class InvalidPassword(error: String) extends ValidateHashError
  final case class InvalidUserName(error: String) extends ValidateHashError
}
