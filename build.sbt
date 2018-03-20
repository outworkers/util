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

lazy val Versions = new {
  val scalatest = "3.0.4"
  val cats = "1.0.1"
  val joda = "2.9.7"
  val jodaConvert = "1.8.1"
  val twitterUtil = "6.41.0"
  val twitterUtil210 = "6.34.0"
  val scalaz = "7.2.8"
  val scalacheck = "1.13.4"
  val datafactory = "0.8"
  val play = "2.6.11"
  val shapeless = "2.3.2"
  val kindProjector = "0.9.3"
  val paradise = "2.1.0"
  val macroCompat = "1.1.1"

  val scala210 = "2.10.6"
  val scala211 = "2.11.11"
  val scala212 = "2.12.4"
  val scalaAll = Seq(scala210, scala211, scala212)

  val scala = new {
    val all = Seq(scala210, scala211, scala212)
  }


  val catsVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => cats
      case _ => twitterUtil210
    }
  }

  val twitterUtilVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor == 12 => twitterUtil
      case _ => twitterUtil210
    }
  }

  val playVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => play
      case _ => "2.4.8"
    }
  }
}

val sharedSettings: Seq[Def.Setting[_]] = Seq(
  organization := "com.outworkers",
  scalaVersion := Versions.scala212,
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
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
) ++ Publishing.effectiveSettings

lazy val baseProjectList: Seq[ProjectReference] = Seq(
  domain,
  parsersCats,
  validatorsCats,
  samplers,
  testing,
  testingTwitter,
  macros,
  tags,
  readme
)

lazy val util = (project in file("."))
  .settings(sharedSettings: _*)
  .settings(
    name := "util",
    moduleName := "util",
    commands += Command.command("testsWithCoverage") { state =>
      "coverage" ::
      "test" ::
      "coverageReport" ::
      "coverageAggregate" ::
      "coveralls" ::
      state
    }
  ).aggregate(
    baseProjectList ++ Publishing.jdk8Only(play): _*
  )

lazy val domain = (project in file("util-domain"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-domain",
    crossScalaVersions := Versions.scala.all
  )

lazy val parsersCats = (project in file("util-parsers-cats"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-parsers-cats",
    crossScalaVersions := Versions.scala.all,
    libraryDependencies ++= Seq(
      "commons-validator"       %  "commons-validator"              % "1.4.0",
      "joda-time"               %  "joda-time"                      % Versions.joda,
      "org.joda"                %  "joda-convert"                   % Versions.jodaConvert,
      "org.typelevel"           %% "cats-core"                      % Versions.cats,
      "org.scalatest"           %% "scalatest"                      % Versions.scalatest % Test
    )
  ).dependsOn(
    domain,
    testing % Test
  )

lazy val tags = (project in file("util-tags"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-tags",
    crossScalaVersions := Versions.scala.all,
    scalacOptions ++= Seq(
      "-language:experimental.macros"
    ),
    libraryDependencies ++= Seq(
      "com.eaio.uuid" % "uuid" % "3.2",
      "org.typelevel" %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.paradise cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % Versions.scalatest % Test
    )
  ).dependsOn(
    domain,
    macros
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
      "org.typelevel" %% "macro-compat" % Versions.macroCompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.paradise cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % Versions.scalatest % Test,
      "org.scalacheck" %% "scalacheck" % Versions.scalacheck
    )
  ).dependsOn(
    domain,
    macros
  )

lazy val testing = (project in file("util-testing"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-testing",
    crossScalaVersions := Versions.scala.all,
    scalacOptions ++= Seq(
      "-language:experimental.macros"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macroCompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.paradise cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % Versions.scalatest,
      "joda-time" % "joda-time" % Versions.joda,
      "org.joda" % "joda-convert" % Versions.jodaConvert,
      "org.scalacheck" %% "scalacheck" % Versions.scalacheck
    )
  ).dependsOn(
    domain,
    tags,
    macros,
    samplers
  )

lazy val testingTwitter = (project in file("util-testing-twitter"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-testing-twitter",
    crossScalaVersions := Versions.scala.all,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % Versions.scalatest,
      "com.twitter" %% "util-core" % Versions.twitterUtilVersion(scalaVersion.value)
    )
  )

lazy val play = (project in file("util-play"))
  .settings(sharedSettings: _*)
  .settings(

    moduleName := "util-play",
    crossScalaVersions := Seq(Versions.scala210, Versions.scala211),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-ws" % Versions.playVersion(scalaVersion.value)
    ),
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile).value / ("scala-2." + {
        CrossVersion.partialVersion(scalaBinaryVersion.value) match {
          case Some((major, minor)) if minor <= 11 => minor.toString
          case _ => "non-existing"
        }
      }))
  ).dependsOn(
  domain,
  parsersCats,
  testing % Test
)

lazy val macros = (project in file("util-macros"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-macros",
    crossScalaVersions := Versions.scala.all,
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.paradise cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
    )
  )

lazy val validatorsCats = (project in file("util-validators-cats"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-validators-cats",
    crossScalaVersions := Versions.scala.all,
    addCompilerPlugin(
      "org.spire-math" % "kind-projector" % Versions.kindProjector cross CrossVersion.binary
    ),
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % Versions.shapeless,
      "org.typelevel" %% "cats-core" % Versions.cats
    )
  ).dependsOn(
    parsersCats,
    testing % Test
  )


lazy val readme = (project in file("readme"))
  .settings(sharedSettings ++ Publishing.noPublishSettings)
  .settings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    tutSourceDirectory := sourceDirectory.value / "main" / "tut",
    tutTargetDirectory := util.base / "docs",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % Versions.macroCompat % "tut",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "tut",
      compilerPlugin("org.scalamacros" % "paradise" % Versions.paradise cross CrossVersion.full),
      "org.scalatest" %% "scalatest" % Versions.scalatest % "tut"
    )
  ).dependsOn(
    domain,
    play,
    parsersCats,
    macros,
    samplers,
    testing,
    validatorsCats
  ).enablePlugins(TutPlugin)
