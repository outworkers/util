/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, _}
import sbtrelease.ReleaseStateTransformations._
import Publishing.{ciSkipSequence, pgpPass, releaseTutFolder, runningUnderCi}

lazy val Versions = new {
  val scalatest = "3.0.8"
  val cats = "1.5.0"
  val joda = "2.10.1"
  val jodaConvert = "2.1.2"
  val scalaz = "7.2.27"
  val scalacheck = "1.14.0"
  val datafactory = "0.8"
  val shapeless = "2.3.3"
  val kindProjector = "0.11.0"
  val paradise = "2.1.1"
  val macroCompat = "1.1.1"

  val scala210 = "2.10.6"
  val scala211 = "2.11.12"
  val scala212 = "2.12.8"
  val scala213 = "2.13.1"
  val scalaAll = Seq(scala211, scala212, scala213)

  val scala = new {
    val all = Seq(scala211, scala212, scala213)
  }

  lazy val ScalacOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-feature",
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  //"-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture" // Turn on future language features.
  //"-Yno-adapted-args" // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
)

val XLintOptions = Seq(
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match" // Pattern match may not be typesafe.
)

val Scala212Options = Seq(
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ypartial-unification", // Enable partial unification in type constructor inference,
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates" // Warn if a private member is unused.
) ++ XLintOptions

val YWarnOptions = Seq(
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

val scalacOptionsFn: String => Seq[String] = { s =>
  CrossVersion.partialVersion(s) match {
    case Some((_, minor)) if minor >= 12 => ScalacOptions ++ YWarnOptions ++ Scala212Options
    case _ => ScalacOptions ++ YWarnOptions
  }
}

scalacOptions in ThisBuild ++= scalacOptionsFn(scalaVersion.value)

  val catsVersion: String => ModuleID = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 13 => "org.typelevel" %% "cats-core" % "2.0.0"
      case Some((_, minor)) if minor >= 11 => "org.typelevel" %% "cats-core" % "1.6.0"
      case _ => "org.typelevel" %% "cats-core" % "1.2.0"
    }
  }

  val scalaMacrosVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => paradise
      case _ => "2.1.0"
    }
  }

   val macroCompatVersion: String => ModuleID = {
     s => CrossVersion.partialVersion(s) match {
       case Some((_, minor)) if minor >= 13 => "org.typelevel" % "macro-compat_2.13.0-RC2" % "1.1.1"
       case Some((_, minor)) if minor < 13 => "org.typelevel" %% "macro-compat" % macroCompat

     }
   }

  val paradiseVersion: String => Seq[ModuleID] = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 13 =>
        Nil
      case Some((_, minor)) if minor < 13 =>
        List(compilerPlugin("org.scalamacros" % "paradise" % scalaMacrosVersion(s) cross CrossVersion.full))
    }
  }
}

val releaseSettings = Seq(
  releaseTutFolder := baseDirectory.value / "docs",
  releaseIgnoreUntrackedFiles := true,
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  releaseTagComment := s"Releasing ${(version in ThisBuild).value} $ciSkipSequence",
  releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} $ciSkipSequence",
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    releaseStepTask((tut in Tut) in readme),
    setReleaseVersion,
    Publishing.commitTutFilesAndVersion,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommandAndRemaining("sonatypeReleaseAll"),
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

val sharedSettings: Seq[Def.Setting[_]] = Seq(
  organization := "com.outworkers",
  scalaVersion := Versions.scala212,
  crossScalaVersions := Versions.scala.all,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
  ),
  scalacOptions ++= Seq(
    "-language:postfixOps",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-unchecked"
  )
) ++ Publishing.effectiveSettings ++ releaseSettings

lazy val baseProjectList: Seq[ProjectReference] = Seq(
  domain,
  parsers,
  parsersCats,
  validatorsCats,
  validators,
  samplers,
  macros,
  readme
)

lazy val util = (project in file("."))
  .settings(sharedSettings ++ Publishing.doNotPublishSettings)
  .settings(
    name := "util",
    moduleName := "util",
    crossScalaVersions := Nil
  ).aggregate(
  baseProjectList: _*
)

lazy val domain = (project in file("util-domain"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-domain",
    crossScalaVersions := Versions.scala.all
  )

lazy val parsers = (project in file("util-parsers"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-parsers",
    crossScalaVersions := Versions.scala.all,
    libraryDependencies ++= Seq(
      "commons-validator"       %  "commons-validator"              % "1.6",
      "joda-time"               %  "joda-time"                      % Versions.joda,
      "org.joda"                %  "joda-convert"                   % Versions.jodaConvert,
      "org.scalaz"              %% "scalaz-core"                    % Versions.scalaz,
      "org.scalatest"           %% "scalatest"                      % Versions.scalatest % Test
    )
  ).dependsOn(
  domain,
  samplers % Test
)

lazy val parsersCats = (project in file("util-parsers-cats"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-parsers-cats",
    crossScalaVersions := Versions.scala.all,
    libraryDependencies ++= Seq(
      "commons-validator"       %  "commons-validator"              % "1.6",
      "joda-time"               %  "joda-time"                      % Versions.joda,
      "org.joda"                %  "joda-convert"                   % Versions.jodaConvert,
      Versions.catsVersion(scalaVersion.value),
      "org.scalatest"           %% "scalatest"                      % Versions.scalatest % Test
    )
  ).dependsOn(
  domain,
  samplers % Test
)

lazy val samplers = (project in file("util-samplers"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-samplers",
    crossScalaVersions := Versions.scala.all,
    scalacOptions ++= Seq(
      "-language:experimental.macros"
    ),
    libraryDependencies ++= Seq(
      Versions.macroCompatVersion(scalaVersion.value),
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "org.scalatest" %% "scalatest" % Versions.scalatest % Test,
      "org.scalacheck" %% "scalacheck" % Versions.scalacheck
    ) ++ Versions.paradiseVersion(scalaVersion.value)
  ).dependsOn(
  domain,
  macros
)

lazy val macros = (project in file("util-macros"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-macros",
    crossScalaVersions := Versions.scala.all,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
    ) ++ Versions.paradiseVersion(scalaVersion.value)
  )

lazy val validatorsCats = (project in file("util-validators-cats"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-validators-cats",
    crossScalaVersions := List(Versions.scala211, Versions.scala212, Versions.scala213),
    addCompilerPlugin(
      "org.typelevel" % "kind-projector" % Versions.kindProjector cross CrossVersion.full
    ),
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % Versions.shapeless,
      "org.scalatest" %% "scalatest" % Versions.scalatest % Test,
      Versions.catsVersion(scalaVersion.value)
    )
  ).dependsOn(
  parsersCats,
  samplers % Test
)

lazy val validators = (project in file("util-validators"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-validators",
    crossScalaVersions := Versions.scala.all,
    addCompilerPlugin(
      "org.typelevel" % "kind-projector" % Versions.kindProjector cross CrossVersion.full
    ),
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % Versions.scalaz,
      "org.scalatest" %% "scalatest" % Versions.scalatest % Test
    )
  ).dependsOn(
  validatorsCats,
  parsers,
  samplers % Test
)

lazy val readme = (project in file("readme"))
  .settings(sharedSettings: _*)
  .dependsOn(
    domain,
    parsers,
    parsersCats,
    validatorsCats,
    validators,
    samplers,
    macros
  ).settings(
    scalaVersion := Versions.scala212,
    crossScalaVersions := Nil,
    tutSourceDirectory := sourceDirectory.value / "main" / "tut",
    tutTargetDirectory := util.base / "docs",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % Versions.scalatest
      )
  ).enablePlugins(TutPlugin)
