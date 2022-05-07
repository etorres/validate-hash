package es.eriktorr.validate_hash
package model

import model.Password.CipherText

import cats.effect.IO

trait Vault:
  def add(username: Username, secret: Password[CipherText]): IO[Unit]
  def passwordFor(username: Username): IO[Option[Password[CipherText]]]
