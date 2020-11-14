package es.eriktorr.validate_hash.application

import cats._
import cats.data._
import cats.derived._
import cats.effect._
import cats.effect.concurrent.Ref
import es.eriktorr.validate_hash.domain.access.Access._
import es.eriktorr.validate_hash.domain.access._
import es.eriktorr.validate_hash.domain.error._
import es.eriktorr.validate_hash.domain.hash.md5Hash
import es.eriktorr.validate_hash.domain.password.Password._
import es.eriktorr.validate_hash.domain.password._
import es.eriktorr.validate_hash.domain.user._
import es.eriktorr.validate_hash.infrastructure.{FakeVault, VaultState}
import es.eriktorr.validate_hash.shared.infrastructure.UserNameGenerator.userNameGen
import es.eriktorr.validate_hash.shared.infrastructure.ValidateAccessGenerator
import es.eriktorr.validate_hash.shared.infrastructure.ValidateAccessGenerator.UserNameWithPassword
import org.scalacheck._
import weaver._
import weaver.scalacheck._

object ValidateAccessSuite extends SimpleIOSuite with IOCheckers {
  final case class TestCase(
    grantedUsers: NonEmptyList[UserNameWithPassword],
    forbiddenUsers: NonEmptyList[UserNameWithPassword],
    invalidUserName: UserName
  )

  object TestCase {
    implicit val showTestCase: Show[TestCase] = semiauto.show
  }

  private[this] val idHash: Password[ClearText] => Either[InvalidPassword, Password[CipherText]] =
    (password: Password[ClearText]) => Password.fromString[CipherText](password.unPassword.value)

  private[this] val md5HashGen = ValidateAccessGenerator.userNameWithPassword(md5Hash)

  private[this] val idHashGen = ValidateAccessGenerator.userNameWithPassword(idHash)

  private[this] val gen = for {
    initGranted <- Gen.containerOfN[List, UserNameWithPassword](3, md5HashGen)
    lastGranted <- md5HashGen
    allGranted = NonEmptyList.ofInitLast(initGranted, lastGranted)
    initForbidden <- Gen.containerOfN[List, UserNameWithPassword](2, idHashGen)
    lastForbidden <- idHashGen
    allForbidden = NonEmptyList.ofInitLast(initForbidden, lastForbidden)
    invalidUserName <- userNameGen
  } yield TestCase(allGranted, allForbidden, invalidUserName)

  simpleTest("Grant access to users authenticated with password") {
    forall(gen) {
      case TestCase(grantedUsers, forbiddenUsers, _) =>
        for {
          vaultStateRef <- Ref.of[IO, VaultState](
            VaultState((grantedUsers.toList ++ forbiddenUsers.toList).toMap)
          )
          validateAccess = ValidateAccess.impl[IO](FakeVault.impl[IO](vaultStateRef), md5Hash)
          (userName, (password, _)) = grantedUsers.head
          result <- validateAccess.grantAccessIdentifiedWith(userName, password)
        } yield expect(result == AccessDecision(userName, Granted))
    }
  }
}
