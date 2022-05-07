package es.eriktorr.validate_hash
package acceptance

import acceptance.ValidateAccessAcceptanceTest.testCaseGen
import application.ValidateAccess
import infrastructure.Generators.{accessGen, passwordGen, usernameGen, User}
import infrastructure.{InMemoryVault, InMemoryVaultState}
import model.*
import model.AccessDecision.Access
import model.Password.{CipherText, ClearText}

import cats.effect.kernel.Ref
import cats.effect.{IO, Resource}
import cats.implicits.*
import es.eriktorr.validate_hash.model.AccessValidationError.InvalidPassword
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.*
import org.scalacheck.effect.PropF.{effectOfPropFToPropF, forAllF}

final class ValidateAccessAcceptanceTest extends CatsEffectSuite with ScalaCheckEffectSuite:

  private[this] val vaultFixture: Fixture[Vault] = ResourceSuiteLocalFixture(
    "vault",
    Resource.eval(Ref.of[IO, InMemoryVaultState](InMemoryVaultState.empty).map(InMemoryVault(_))),
  )

  override def munitFixtures: Seq[Fixture[?]] = List(vaultFixture)

  test("it should validate access") {
    forAllF(testCaseGen) { testCase =>
      for
        _ <- testCase.registeredUsers.traverse_(vaultFixture().add)
        accessDecision <- ValidateAccess
          .impl(vaultFixture(), Password.cipher)
          .grantAccessIdentifiedBy(testCase.username, testCase.password)
      yield assertEquals(accessDecision, testCase.expectedAccessDecision)
    }
  }

  test("fail when the hash function cannot digest a password") {
    interceptMessageIO[InvalidPassword]("You need to stop YOLO-ing")(
      ValidateAccess
        .impl(vaultFixture(), _ => Left(InvalidPassword("You need to stop YOLO-ing")))
        .grantAccessIdentifiedBy(
          Username.unsafeFrom("Jane Doe"),
          Password.unsafeFrom[ClearText]("YOLO"),
        ),
    )
  }

object ValidateAccessAcceptanceTest:
  final private case class TestCase(
      username: Username,
      password: Password[ClearText],
      expectedAccessDecision: AccessDecision,
      registeredUsers: List[(Username, Password[CipherText])],
  )

  private val testCaseGen: Gen[TestCase] =
    for
      usernames <- Gen.nonEmptyContainerOf[Set, Username](usernameGen).map(_.toList)
      selectedUser +: otherUsers <- usernames.traverse(username =>
        for
          password <- passwordGen[ClearText]
          (hash, access) <- accessGen(password)
        yield User(username, password, hash, access),
      )
      registeredUsers = (selectedUser :: otherUsers)
        .map { x =>
          x.secret match
            case Some(secret) => Some(x.username, secret)
            case None => Option.empty
        }
        .collect { case Some((username, secret)) =>
          (username, secret)
        }
    yield TestCase(
      selectedUser.username,
      selectedUser.password,
      AccessDecision(selectedUser.username, selectedUser.access),
      registeredUsers,
    )
