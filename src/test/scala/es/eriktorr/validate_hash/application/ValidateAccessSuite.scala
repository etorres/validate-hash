package es.eriktorr.validate_hash.application

import cats._
import cats.data._
import cats.derived._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import es.eriktorr.validate_hash.domain.access.Access._
import es.eriktorr.validate_hash.domain.access._
import es.eriktorr.validate_hash.domain.error._
import es.eriktorr.validate_hash.domain.hash.md5Hash
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.user._
import es.eriktorr.validate_hash.infrastructure.{FakeVault, VaultState}
import es.eriktorr.validate_hash.shared.infrastructure.ValidateAccessGenerator.{
  distinctUserNameGen,
  passwordGen,
  userNameWithPasswordGen,
  UserNameWithPassword
}
import org.scalacheck._
import org.scalacheck.cats.implicits._
import weaver._
import weaver.scalacheck._

object ValidateAccessSuite extends SimpleIOSuite with IOCheckers {
  final case class TestCase(
    grantedUsers: NonEmptyList[UserNameWithPassword],
    forbiddenUsers: NonEmptyList[UserNameWithPassword],
    allValidUsers: Map[UserName, (Password[ClearText], Password[CipherText])],
    invalidUserNames: NonEmptyList[UserNameWithPassword]
  )

  object TestCase {
    implicit val showTestCase: Show[TestCase] = semiauto.show
  }

  private[this] val idHash: Password[ClearText] => Either[InvalidPassword, Password[CipherText]] =
    (password: Password[ClearText]) => Password.fromString[CipherText](password.unPassword.value)

  private[this] val gen = for {
    userNames <- distinctUserNameGen(7)
    (grantedUserNames, otherUserNames) = userNames.splitAt(3)
    (forbiddenUserNames, invalidUserNames) = otherUserNames.splitAt(2)
    grantedUsers <- grantedUserNames.traverse { userName =>
      userNameWithPasswordGen(Gen.const(userName), passwordGen(md5Hash))
    }
    forbiddenUsers <- forbiddenUserNames.traverse { userName =>
      userNameWithPasswordGen(Gen.const(userName), passwordGen(idHash))
    }
    invalidUser <- invalidUserNames.traverse { userName =>
      userNameWithPasswordGen(Gen.const(userName), passwordGen(md5Hash))
    }
  } yield TestCase(
    NonEmptyList.fromListUnsafe(grantedUsers),
    NonEmptyList.fromListUnsafe(forbiddenUsers),
    (grantedUsers ++ forbiddenUsers).toMap,
    NonEmptyList.fromListUnsafe(invalidUser)
  )

  def expecting(
    allValidUsers: Map[UserName, (Password[ClearText], Password[CipherText])],
    targetUsers: NonEmptyList[UserNameWithPassword],
    access: Access
  ): IO[Expectations] =
    for {
      vaultStateRef <- Ref.of[IO, VaultState](VaultState(allValidUsers))
      validateAccess = ValidateAccess.impl[IO](FakeVault.impl[IO](vaultStateRef), md5Hash)
      (userName, (password, _)) = targetUsers.head
      result <- validateAccess.grantAccessIdentifiedWith(userName, password)
    } yield expect(result == AccessDecision(userName, access))

  simpleTest("Grant access to users authenticated with password") {
    forall(gen) {
      case TestCase(grantedUsers, _, allValidUsers, _) =>
        expecting(allValidUsers, grantedUsers, Granted)
    }
  }

  simpleTest("Forbid access to users with invalid password") {
    forall(gen) {
      case TestCase(_, forbiddenUsers, allValidUsers, _) =>
        expecting(allValidUsers, forbiddenUsers, Forbidden)
    }
  }

  simpleTest("Forbid access to unknown users") {
    forall(gen) {
      case TestCase(_, _, allValidUsers, invalidUsers) =>
        expecting(allValidUsers, invalidUsers, Forbidden)
    }
  }
}
