import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys._
import sbt._
import wartremover.Wart
import wartremover.WartRemover.autoImport._

name := "validate-hash"

ThisBuild / organization := "es.eriktorr"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.13.6"

ThisBuild / idePackagePrefix := Some("es.eriktorr.validate_hash")
Global / excludeLintKeys += idePackagePrefix

val catsCoreVersion = "2.6.1"
val catsEffectsVersion = "2.5.3"
val catsScalacheckVersion = "0.3.1"
val enumeratumVersion = "1.7.0"
val kittensVersion = "2.3.2"
val newtypeVersion = "0.4.4"
val refinedVersion = "0.9.27"
val weaverVersion = "0.6.6"

ThisBuild / libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1" cross CrossVersion.binary),
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectsVersion,
  "org.tpolecat" %% "typename" % "1.0.0",
  "io.chrisdavenport" %% "cats-scalacheck" % catsScalacheckVersion % Test,
  "com.beachape" %% "enumeratum-cats" % enumeratumVersion,
  "org.typelevel" %% "kittens" % kittensVersion,
  "io.estatico" %% "newtype" % newtypeVersion,
  "eu.timepit" %% "refined" % refinedVersion,
  "eu.timepit" %% "refined-scalacheck" % refinedVersion % Test,
  "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test,
  "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test
)

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.typelevel" %% "cats-core" % VersionScheme.EarlySemVer,
  "org.typelevel" %% "cats-effect" % VersionScheme.EarlySemVer,
  "co.fs2" %% "f2s-core" % VersionScheme.EarlySemVer,
  "org.scalacheck" %% "scalacheck" % VersionScheme.EarlySemVer
)

ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Xfatal-warnings",
  "-Xlint",
  "-Xlint:-byname-implicit",
  "-Ymacro-annotations",
  "-deprecation",
  "-unchecked",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-feature"
)

ThisBuild / javacOptions ++= Seq(
  "-g:none",
  "-source",
  "11",
  "-target",
  "11",
  "-encoding",
  "UTF-8"
)

ThisBuild / scalafmtOnCompile := true

val warts: Seq[Wart] = Warts.allBut(
  Wart.Any,
  Wart.Nothing,
  Wart.Equals,
  Wart.DefaultArguments,
  Wart.Overloading,
  Wart.ToString,
  Wart.ImplicitParameter,
  Wart.ImplicitConversion // @newtype
)

Compile / compile / wartremoverErrors ++= warts
Test / compile / wartremoverErrors ++= warts

ThisBuild / testFrameworks += new TestFramework("weaver.framework.CatsEffect")
