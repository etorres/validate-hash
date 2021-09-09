package es.eriktorr.validate_hash
package domain

import domain.user._

import enumeratum._

object access {
  final case class AccessDecision(userName: UserName, access: Access)

  sealed trait Access extends EnumEntry with Product with Serializable

  object Access extends Enum[Access] with CatsEnum[Access] {
    case object Granted extends Access
    case object Forbidden extends Access

    override def values: IndexedSeq[Access] = findValues
  }
}
