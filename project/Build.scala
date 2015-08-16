/*
 * Copyright 2013-2015 Websudos, Limited.
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
import sbt.Keys._
import sbt._
import com.twitter.sbt._

object Build extends Build {

	val ScalaTestVersion = "2.2.4"
  val FinagleVersion = "6.25.0"
  val FinagleZkVersion = "6.24.0"
  val TwitterUtilVersion = "6.24.0"
  val LiftVersion = "3.0-M1"
  val ScalazVersion = "7.1.0"
  val JodaTimeVersion = "2.3"

  def liftVersion(scalaVersion: String) = {
    (scalaVersion match {
      case "2.10.5" => "net.liftweb" % "lift-webkit_2.10" % LiftVersion
      case _ => "net.liftweb" % "lift-webkit_2.11" % "3.0-M2"
    }) % "compile"
  }

  val bintrayPublishSettings : Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintray.BintrayKeys.bintrayReleaseOnPublish in ThisBuild := true,
    bintray.BintrayKeys.bintrayOrganization := Some("websudos"),
    bintray.BintrayKeys.bintrayRepository := "oss-releases",
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )

  val mpublishSettings : Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo <<= version.apply {
      v =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true },
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),
    pomExtra :=
      <url>https://github.com/websudos/util</url>
        <scm>
          <url>git@github.com:websudos/util.git</url>
          <connection>scm:git:git@github.com:websudos/util.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alexflav23</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
  )

  val sharedSettings: Seq[Def.Setting[_]] = Seq(
		organization := "com.websudos",
    version := "0.9.11",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.5", "2.11.7"),
		resolvers ++= Seq(
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
      Resolver.bintrayRepo("websudos", "oss-releases")
		),
		scalacOptions ++= Seq(
      "-language:postfixOps",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-unchecked"
		),
    libraryDependencies ++= Seq(
      "org.scalatest"           %% "scalatest" % ScalaTestVersion % "test, provided"
    )
	) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    GitProject.gitSettings ++
    mpublishSettings ++
    VersionManagement.newSettings


	lazy val websudosUtil = Project(
		id = "util",
		base = file("."),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).aggregate(
    UtilAws,
		UtilCore,
    UtilDomain,
    UtilHttp,
    UtilLift,
    UtilParsers,
    UtilZooKeeper,
    UtilTesting
	)

	lazy val UtilCore = Project(
		id = "util-core",
		base = file("util-core"),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).settings(
		name := "util-core",
    libraryDependencies ++= Seq(
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion % "test, provided"
    )
	)

  lazy val UtilHttp = Project(
    id = "util-http",
    base = file("util-http"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-http",
    libraryDependencies ++= Seq(
      "com.twitter"             %% "finagle-http"                   % FinagleVersion
    )
  )

  lazy val UtilParsers = Project(
    id = "util-parsers",
    base = file("util-parsers"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-parsers",
    libraryDependencies ++= Seq(
      "commons-validator"       %  "commons-validator"              % "1.4.0",
      "joda-time"               %  "joda-time"                      % JodaTimeVersion,
      "org.joda"                %  "joda-convert"                   % "1.6",
      "org.scalaz"              %% "scalaz-core"                    % ScalazVersion,
      "org.scalatest"           %% "scalatest"                      % ScalaTestVersion % "test, provided"
    )
  ).dependsOn(
    UtilDomain,
    UtilHttp,
    UtilTesting % "test, provided"
  )

  lazy val UtilLift = Project(
    id = "util-lift",
    base = file("util-lift"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-lift",
    libraryDependencies <++= scalaVersion (sv => Seq(liftVersion(sv)))

  ).dependsOn(
    UtilParsers,
    UtilTesting % "test, provided"
  )

  lazy val UtilAws = Project(
    id = "util-aws",
    base = file("util-aws"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-aws",
    libraryDependencies ++= Seq(
      "com.twitter"             %% "finagle-http"                      % FinagleVersion
    )
  ).dependsOn(
    UtilHttp,
    UtilTesting % "test, provided"
  )

  lazy val UtilZooKeeper = Project(
    id = "util-zookeeper",
    base = file("util-zookeeper"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-zookeeper",
    libraryDependencies ++= Seq(
      "com.twitter"                     %% "finagle-zookeeper"        % FinagleZkVersion,
      "com.twitter"                      %% "finagle-serversets"       % FinagleVersion
    )
  ).dependsOn(
    UtilTesting % "test"
  )

  lazy val UtilDomain = Project(
    id = "util-domain",
    base = file("util-domain"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-domain"
  )

  lazy val UtilTesting = Project(
    id = "util-testing",
    base = file("util-testing"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-testing",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "util-core"                % TwitterUtilVersion,
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion,
      "joda-time"                        %  "joda-time"                % JodaTimeVersion,
      "org.joda"                         %  "joda-convert"             % "1.6",
      "org.scalacheck"                   %% "scalacheck"               % "1.11.4",
      "org.fluttercode.datafactory"      %  "datafactory"              % "0.8"
    )
  ).dependsOn(
    UtilDomain
  )
}
