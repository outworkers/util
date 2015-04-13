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

object UtilBuild extends Build {

  val NettyVersion = "3.9.0.Final"
	val ScalaTestVersion = "2.2.0-M1"
  val FinagleVersion = "6.24.0"
  val LiftVersion = "2.6-M4"
  val ScalazVersion = "7.1.0"
  val JodaTimeVersion = "2.3"

  def liftVersion(scalaVersion: String) = {
    (scalaVersion match {
      case "2.10.4" => "net.liftweb" % "lift-webkit_2.10" % LiftVersion
      case _ => "net.liftweb" % "lift-webkit_2.11" % "3.0-M2"
    }) % "compile"
  }

  val publishUrl = "http://maven.websudos.co.uk"

  val publishSettings : Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    crossScalaVersions := Seq("2.10.4", "2.11.5"),
    publishTo <<= version { (v: String) => {
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at publishUrl + "/ext-snapshot-local")
      else
        Some("releases"  at publishUrl + "/ext-release-local")
    }
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true }
  )

  val mavenPublishSettings : Seq[Def.Setting[_]] = Seq(
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
    pomExtra :=
      <url>https://github.com/websudos/util</url>
        <licenses>
          <license>
            <name>Apache License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:websudos/util.git</url>
          <connection>scm:git:git@github.com:websudos/util.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alexflav</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
  )

  val sharedSettings: Seq[Def.Setting[_]] = Seq(
		organization := "com.websudos",
    version := "0.7.0",
    scalaVersion := "2.11.5",
		resolvers ++= Seq(
		"Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
		"Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
		"Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
		"Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
		"Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
		"Twitter Repository"               at "http://maven.twttr.com",
    "newzly External snapshots"        at "http://newzly-artifactory.elasticbeanstalk.com/ext-release-local",
    "newzly External"                  at "http://newzly-artifactory.elasticbeanstalk.com/ext-snapshot-local"
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
	) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ publishSettings


	lazy val websudosUtil = Project(
		id = "util",
		base = file("."),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).aggregate(
    websudosUtilAws,
		websudosUtilCore,
    websudosUtilHttp,
    websudosUtilLift,
    websudosUtilParsers,
    websudosZooKeeper,
    websudosUtilTesting
	)

	lazy val websudosUtilCore = Project(
		id = "util-core",
		base = file("util-core"),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).settings(
		name := "util-core",
    libraryDependencies ++= Seq(
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion % "test, provided"
    )
	)

  lazy val websudosUtilHttp = Project(
    id = "util-http",
    base = file("util-http"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-http",
    libraryDependencies ++= Seq(
      "com.twitter"             %% "finagle-http"                   % FinagleVersion,
      "io.netty"                % "netty"                           % NettyVersion
    )
  )

  lazy val websudosUtilParsers = Project(
    id = "util-parsers",
    base = file("util-parsers"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-parsers",
    libraryDependencies ++= Seq(
      "commons-validator"       % "commons-validator"               % "1.4.0",
      "joda-time"               %  "joda-time"                      % JodaTimeVersion,
      "org.joda"                %  "joda-convert"                   % "1.6",
      "org.scalaz"              %% "scalaz-core"                    % ScalazVersion,
      "org.scalatest"           %% "scalatest"                      % ScalaTestVersion % "test, provided"
    )
  ).dependsOn(
    websudosUtilHttp,
    websudosUtilTesting % "test, provided"
  )

  lazy val websudosUtilLift = Project(
    id = "util-lift",
    base = file("util-lift"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-lift",
    libraryDependencies <++= scalaVersion (sv => Seq(liftVersion(sv)))
  ).dependsOn(
    websudosUtilParsers,
    websudosUtilTesting % "test, provided"
  )

  lazy val websudosUtilAws = Project(
    id = "util-aws",
    base = file("util-aws"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-aws",
    libraryDependencies ++= Seq(
      "com.twitter"             %% "finagle-http"                      % FinagleVersion
    )
  ).dependsOn(
    websudosUtilHttp,
      websudosUtilTesting % "test, provided"
  )

  lazy val websudosZooKeeper = Project(
    id = "util-zookeeper",
    base = file("util-zookeeper"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-zookeeper",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "finagle-zookeeper"        % FinagleVersion,
      "com.twitter"                      %% "finagle-serversets"       % FinagleVersion
    )
  ).dependsOn(
      websudosUtilTesting % "test"
  )

  lazy val websudosUtilTesting = Project(
    id = "util-testing",
    base = file("util-testing"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    name := "util-testing",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "util-core"                % "6.23.0",
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion,
      "joda-time"                        %  "joda-time"                % JodaTimeVersion,
      "org.joda"                         %  "joda-convert"             % "1.6",
      "org.scalacheck"                   %% "scalacheck"               % "1.11.4",
      "org.fluttercode.datafactory"      %  "datafactory"              % "0.8"
    )
  )
}
