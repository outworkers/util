import sbt._
import Keys._
import com.twitter.sbt._

object util extends Build {

  val nettyVersion = "3.9.0.Final"
	val scalatestVersion = "2.2.0-M1"
  val finagleVersion = "6.17.0"
  val liftVersion = "2.6-M2"
  val phantomVersion = "0.3.2"

  val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
    publishTo := Some("newzly releases" at "http://maven.newzly.com/repository/internal"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true }
  )

	val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
		organization := "com.newzly",
		version := "0.1.17",
		scalaVersion := "2.10.4",
		resolvers ++= Seq(
		"Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
		"Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
		"Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
		"Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
		"Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
		"Twitter Repository"               at "http://maven.twttr.com",
    "newzly snapshots"                 at "http://maven.newzly.com/repository/snapshots",
    "newzly repository"                at "http://maven.newzly.com/repository/internal"
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
	) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings


	lazy val newzlyUtil = Project(
		id = "util",
		base = file("."),
		settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
	).aggregate(
    newzlyUtilAws,
		newzlyUtilCore,
    newzlyUtilHttp,
    newzlyUtilLift,
		newzlyUtilTest,
    newzlyUtilTesting,
    newzlyUtilTestingCassandra
	)

	lazy val newzlyUtilCore = Project(
		id = "util-core",
		base = file("util-core"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
	).settings(
		name := "util-core"
	)

  lazy val newzlyUtilHttp = Project(
    id = "util-http",
    base = file("util-http"),
    settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
  ).settings(
    name := "util-http",
    libraryDependencies ++= Seq(
      "io.netty"        % "netty"                % nettyVersion
    )
  )

  lazy val newzlyUtilLift = Project(
    id = "util-lift",
    base = file("util-lift"),
    settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
  ).settings(
    name := "util-lift",
    libraryDependencies ++= Seq(
      "net.liftweb"             %% "lift-webkit"                    % liftVersion % "compile"
    )
  )

  lazy val newzlyUtilAws = Project(
    id = "util-aws",
    base = file("util-aws"),
    settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
  ).settings(
    name := "util-aws",
    libraryDependencies ++= Seq(
      "com.twitter"             %% "finagle"                           % finagleVersion,
      "com.twitter"             %% "finagle-core"                      % finagleVersion exclude("org.slf4j", "slf4j"),
      "com.twitter"             %% "finagle-http"                      % finagleVersion
    )
  ).dependsOn(
    newzlyUtilHttp
  )

  lazy val newzlyUtilTesting = Project(
    id = "util-testing",
    base = file("util-testing"),
    settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
  ).settings(
    name := "util-testing",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "util-core"                % finagleVersion,
      "org.scalatest"                    %% "scalatest"                % scalatestVersion,
      "org.scalacheck"                   %% "scalacheck"               % "1.11.3"              % "test",
      "org.fluttercode.datafactory"      %  "datafactory"              % "0.8"
    )
  ).dependsOn(
    newzlyUtilHttp
  )

  lazy val newzlyUtilTestingCassandra = Project(
    id = "util-testing-cassandra",
    base = file("util-testing-cassandra"),
    settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
  ).settings(
    name := "util-testing-cassandra",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "finagle-serversets"       % finagleVersion,
      "com.twitter"                      %% "finagle-zookeeper"        % finagleVersion,
      "org.cassandraunit"                %  "cassandra-unit"           % "2.0.2.1"  exclude("log4j", "log4j"),
      "org.scalatest"                    %% "scalatest"                % scalatestVersion       % "test"
    )
  ).dependsOn(
    newzlyUtilTesting
  )

	lazy val newzlyUtilTest = Project(
		id = "util-test",
		base = file("util-test"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
	).settings(
		name := "util-test",
		libraryDependencies ++= Seq(
			"org.scalatest"           %% "scalatest"                          % scalatestVersion % "provided"
		)
	).dependsOn(
		newzlyUtilCore
	)

}
