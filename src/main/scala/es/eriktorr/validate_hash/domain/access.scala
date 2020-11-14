package es.eriktorr.validate_hash.domain

import enumeratum._
import es.eriktorr.validate_hash.domain.user._

object access {
  final case class AccessDecision(userName: UserName, access: Access)

  sealed trait Access extends EnumEntry with Product with Serializable

  object Access extends Enum[Access] with CatsEnum[Access] {
    case object Granted extends Access
    case object Forbidden extends Access

    override def values: IndexedSeq[Access] = findValues
  }
}
