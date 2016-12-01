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
import com.twitter.sbt.{GitProject, VersionManagement}

lazy val Versions = new {
  val scalatest = "3.0.0"
  val cats = "0.7.2"
  val finagle = "6.36.0"
  val joda = "2.9.4"
  val jodaConvert = "1.8.1"
  val lift = "3.0-RC3"
  val twitterUtil = "6.33.0"
  val scalaz = "7.3.0-M7"
  val finagleZk = "6.24.0"
  val scalacheck = "1.13.2"
  val datafactory = "0.8"
  val play = "2.5.8"

  val playVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((major, minor)) if minor >= 11 => play
      case _ => "2.4.8"
    }
  }

  val liftVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((major, minor)) if minor >= 11 => lift
      case _ => "3.0-M1"
    }
  }
}

val sharedSettings: Seq[Def.Setting[_]] = Seq(
  organization := "com.outworkers",
  scalaVersion := "2.12.0",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo
  ),
  gitTagName in ThisBuild <<= (organization, name, version) map { (o, n, v) => s"version=$v"},
  scalacOptions ++= Seq(
    "-language:postfixOps",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-unchecked"
  )
) ++ GitProject.gitSettings ++
  VersionManagement.newSettings ++
  Publishing.effectiveSettings

lazy val baseProjectList: Seq[ProjectReference] = Seq(
  domain,
  lift,
  parsers,
  parsersCats,
  validators,
  testing,
  macros,
  urls
)

lazy val util = (project in file("."))
  .settings(sharedSettings: _*)
  .settings(
    name := "util",
    moduleName := "util"
  ).aggregate(
    baseProjectList ++ Publishing.jdk8Only(play): _*
  )

lazy val urls = (project in file("util-urls"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-urls"
  )

lazy val domain = (project in file("util-domain"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-domain"
  )

lazy val parsers = (project in file("util-parsers"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-parsers",
    libraryDependencies ++= Seq(
      "commons-validator"       %  "commons-validator"              % "1.4.0",
      "joda-time"               %  "joda-time"                      % Versions.joda,
      "org.joda"                %  "joda-convert"                   % Versions.jodaConvert,
      "org.scalaz"              %% "scalaz-core"                    % Versions.scalaz,
      "org.scalatest"           %% "scalatest"                      % Versions.scalatest % Test
    )
  ).dependsOn(
    domain,
    testing % Test
  )

lazy val parsersCats = (project in file("util-parsers-cats"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-parsers-cats",
    libraryDependencies ++= Seq(
      "commons-validator"       %  "commons-validator"              % "1.4.0",
      "joda-time"               %  "joda-time"                      % Versions.joda,
      "org.joda"                %  "joda-convert"                   % Versions.jodaConvert,
      "org.typelevel"           %% "cats"                           % Versions.cats,
      "org.scalatest"           %% "scalatest"                      % Versions.scalatest % Test
    )
  ).dependsOn(
    domain,
    testing % Test
  )

lazy val play = (project in file("util-play"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-play",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-ws" % Versions.playVersion(scalaVersion.value)
    )
  ).dependsOn(
    domain,
    parsersCats,
    testing % Test
  )

lazy val testing = (project in file("util-testing"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-testing",
    scalacOptions ++= Seq(
      "-language:experimental.macros"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      "com.twitter"                      %% "util-core"                % Versions.twitterUtil,
      "org.scalatest"                    %% "scalatest"                % Versions.scalatest,
      "joda-time"                        %  "joda-time"                % Versions.joda,
      "org.joda"                         %  "joda-convert"             % Versions.jodaConvert,
      "org.scalacheck"                   %% "scalacheck"               % Versions.scalacheck,
      "org.fluttercode.datafactory"      %  "datafactory"              % Versions.datafactory
    )
  ).dependsOn(
    domain,
    macros
  )

lazy val lift = (project in file("util-lift"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-lift",
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile).value / ("scala-2." + {
        CrossVersion.partialVersion(scalaBinaryVersion.value) match {
          case Some((major, minor)) => minor
          case _ => "2.10"
        }
    })),
    libraryDependencies ++= Seq(
      "net.liftweb" %% "lift-webkit" % Versions.liftVersion(scalaVersion.value)
    )
  ).dependsOn(
    parsers,
    testing % Test
  )


lazy val macros = (project in file("util-macros"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-validators",
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
    )
  )

lazy val validators = (project in file("util-validators"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "util-validators",
    addCompilerPlugin(
      "org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats" % Versions.cats
    )
).dependsOn(
  parsers,
  testing % Test,
  lift % Test
)
