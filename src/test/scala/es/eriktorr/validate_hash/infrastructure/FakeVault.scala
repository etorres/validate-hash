package es.eriktorr.validate_hash.infrastructure

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.user._
import es.eriktorr.validate_hash.domain.vault._

final case class VaultState(users: Map[UserName, (Password[ClearText], Password[CipherText])])

final class FakeVault[F[_]: Sync] private[infrastructure] (val ref: Ref[F, VaultState])
    extends Vault[F] {
  override def passwordFor(userName: UserName): F[Option[Password[CipherText]]] =
    ref.get.map(_.users.get(userName).map { case (_, passwordHash) => passwordHash })
}

object FakeVault {
  def impl[F[_]: Sync](ref: Ref[F, VaultState]): Vault[F] = new FakeVault[F](ref)

  def initialState: VaultState =
    VaultState(
      (List("Lisa", "Bart") zip List(
        ("jazz", "43e5212ec38f68b6d987feed0268f4a0"),
        ("Krusty", "c56b05ad43e2ee36f6169fe0d0b26bd2")
      )).map(fromString).toMap
    )

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def fromString(
    entry: (String, (String, String))
  ): (UserName, (Password[ClearText], Password[CipherText])) = entry match {
    case (userNameStr, (passwordStr, hashStr)) =>
      (for {
        (userName, password, hash) <- (
          UserName.fromString(userNameStr),
          Password.fromString[ClearText](passwordStr),
          Password.fromString[CipherText](hashStr)
        ).tupled
      } yield (userName, (password, hash))).toOption.get
  }
}
