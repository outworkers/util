import sbt._
import Keys._
import com.twitter.sbt._

object util extends Build {

  val nettyVersion = "3.9.0.Final"
	val scalatestVersion = "2.2.0-M1"
  val finagleVersion = "6.17.0"
  val liftVersion = "2.6-M2"
  val phantomVersion = "0.3.2"

  val publishUrl = "http://newzly-artifactory.elasticbeanstalk.com"

  val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
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


  val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
		organization := "com.newzly",
		version := "0.1.18",
		scalaVersion := "2.10.4",
		resolvers ++= Seq(
		"Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
		"Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
		"Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
		"Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
		"Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
		"Twitter Repository"               at "http://maven.twttr.com",
    "newzly Libs snapshots"            at "http://newzly-artifactory.elasticbeanstalk.com/libs-release-local",
    "newzly Libs"                      at "http://newzly-artifactory.elasticbeanstalk.com/libs-snapshot-local",
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
    newzlyUtilTesting
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
