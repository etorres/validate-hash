package es.eriktorr.validate_hash
package application

import domain.access.Access._
import domain.access._
import domain.hash.md5Hash
import domain.password.Password._
import domain.password._
import domain.user._
import infrastructure.{FakeVault, VaultState}
import shared.infrastructure.ValidateAccessGenerator.{
  distinctUserNameGen,
  passwordGen,
  userNameWithPasswordGen,
  UserNameWithPassword
}

import cats._
import cats.data._
import cats.derived._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import org.scalacheck.cats.implicits._
import weaver._
import weaver.scalacheck._

object ValidateAccessSuite extends SimpleIOSuite with Checkers {

  final case class TestCase(
    grantedUsers: NonEmptyList[UserNameWithPassword],
    forbiddenUsers: NonEmptyList[UserNameWithPassword],
    allValidUsers: Map[UserName, (Password[ClearText], Password[CipherText])],
    invalidUserNames: NonEmptyList[UserNameWithPassword]
  )

  object TestCase {
    implicit val showTestCase: Show[TestCase] = semiauto.show
  }

  private[this] val validPasswordGen = passwordGen(md5Hash)

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  private[this] val invalidPasswordGen = passwordGen((password: Password[ClearText]) =>
    md5Hash(Password.fromString[ClearText](password.unPassword.value + "__").toOption.get)
  )

  private[this] val gen = for {
    userNames <- distinctUserNameGen(7)
    (grantedUserNames, otherUserNames) = userNames.splitAt(3)
    (forbiddenUserNames, invalidUserNames) = otherUserNames.splitAt(2)
    grantedUsers <- grantedUserNames.traverse(userNameWithPasswordGen(_, validPasswordGen))
    forbiddenUsers <- forbiddenUserNames.traverse(userNameWithPasswordGen(_, invalidPasswordGen))
    invalidUser <- invalidUserNames.traverse(userNameWithPasswordGen(_, validPasswordGen))
  } yield TestCase(
    grantedUsers = NonEmptyList.fromListUnsafe(grantedUsers),
    forbiddenUsers = NonEmptyList.fromListUnsafe(forbiddenUsers),
    allValidUsers = (grantedUsers ++ forbiddenUsers).toMap,
    invalidUserNames = NonEmptyList.fromListUnsafe(invalidUser)
  )

  def expecting(
    allValidUsers: Map[UserName, (Password[ClearText], Password[CipherText])],
    targetUsers: NonEmptyList[UserNameWithPassword],
    access: Access
  ): IO[Expectations] =
    for {
      vaultStateRef <- Ref.of[IO, VaultState](VaultState(allValidUsers))
      validateAccess = ValidateAccess.impl[IO](FakeVault.impl[IO](vaultStateRef), md5Hash)
      result <- targetUsers.traverse {
        case (userName, (password, _)) =>
          validateAccess.grantAccessIdentifiedWith(userName, password)
      }
      expected = targetUsers.map {
        case (userName, _) => AccessDecision(userName, access)
      }
    } yield expect(result === expected)

  test("Grant access to users authenticated with password") {
    forall(gen) {
      case TestCase(grantedUsers, _, allValidUsers, _) =>
        expecting(allValidUsers, grantedUsers, Granted)
    }
  }

  test("Forbid access to users with invalid password") {
    forall(gen) {
      case TestCase(_, forbiddenUsers, allValidUsers, _) =>
        expecting(allValidUsers, forbiddenUsers, Forbidden)
    }
  }

  test("Forbid access to unknown users") {
    forall(gen) {
      case TestCase(_, _, allValidUsers, invalidUsers) =>
        expecting(allValidUsers, invalidUsers, Forbidden)
    }
  }

}
