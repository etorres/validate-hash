package es.eriktorr.validate_hash
package model

import model.AccessDecision.Access

final case class AccessDecision(username: Username, access: Access)

object AccessDecision:
  enum Access:
    case Granted, Forbidden
