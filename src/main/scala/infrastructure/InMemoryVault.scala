package es.eriktorr.validate_hash
package infrastructure

import model.Password.CipherText
import model.{Password, Username, Vault}

import cats.effect.{IO, Ref}

final case class InMemoryVaultState(users: Map[Username, Password[CipherText]])

object InMemoryVaultState:
  def empty: InMemoryVaultState = InMemoryVaultState(Map.empty)

final class InMemoryVault(stateRef: Ref[IO, InMemoryVaultState]) extends Vault:
  override def add(username: Username, secret: Password[CipherText]): IO[Unit] =
    stateRef.update(currentState => currentState.copy(currentState.users + (username -> secret)))

  override def passwordFor(username: Username): IO[Option[Password[CipherText]]] =
    stateRef.get.map(_.users.get(username))
