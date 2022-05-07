package es.eriktorr.validate_hash
package model

import scala.util.control.NoStackTrace

@SuppressWarnings(Array("org.wartremover.warts.Null"))
sealed abstract class AccessValidationError(
    message: String,
    cause: Option[Throwable] = Option.empty[Throwable],
) extends NoStackTrace:
  import scala.language.unsafeNulls
  override def getCause: Throwable = cause.orNull
  override def getMessage: String = message

object AccessValidationError:
  final case class InvalidPassword(message: String) extends AccessValidationError(message)
  final case class InvalidUsername(message: String) extends AccessValidationError(message)
