/*
 * Copyright 2013-2016 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
import com.twitter.sbt.{GitProject, VersionManagement}

lazy val Versions = new {
  val scalatest = "2.2.5"
  val cats = "0.7.2"
  val finagle = "6.36.0"
  val joda = "2.9.4"
  val jodaConvert = "1.8.1"
  val lift = "3.0-RC3"
  val twitterUtil = "6.33.0"
  val scalaz = "7.2.6"
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
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  resolvers ++= Seq(
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo,
    Resolver.bintrayRepo("websudos", "oss-releases")
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
    moduleName := "util",
    pgpPassphrase := Publishing.pgpPass
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
    testing
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
    testing
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
        }
    })),
    libraryDependencies ++= Seq(
      "net.liftweb" %% "lift-webkit" % Versions.liftVersion(scalaVersion.value)
    )
  ).dependsOn(
    parsers
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
      "org.spire-math" % "kind-projector" % "0.8.0" cross CrossVersion.binary
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats" % Versions.cats
    )
).dependsOn(
  parsers,
  testing % Test,
  lift % Test
)
