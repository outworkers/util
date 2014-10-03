import sbt._
import Keys._
import com.twitter.sbt._

object UtilBuild extends Build {

  val nettyVersion = "3.9.0.Final"
	val ScalaTestVersion = "2.2.0-M1"
  val FinagleVersion = "6.20.0"
  val LiftVersion = "2.6-M3"
  val phantomVersion = "1.2.8"
  val ScalazVersion = "7.1.0"
  val JodaTimeVersion = "2.3"

  val publishUrl = "http://maven.websudos.co.uk"

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
		organization := "com.websudos",
		version := "0.2.3",
		scalaVersion := "2.10.4",
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
      "org.scalatest"           %% "scalatest"                          % ScalaTestVersion % "test, provided"
    )
	) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ VersionManagement.newSettings ++ publishSettings


	lazy val websudosUtil = Project(
		id = "util",
		base = file("."),
		settings = Project.defaultSettings ++ sharedSettings
	).aggregate(
    websudosUtilAws,
		websudosUtilCore,
    websudosUtilHttp,
    websudosUtilLift,
    websudosUtilParsers,
    websudosUtilTesting
	)

	lazy val websudosUtilCore = Project(
		id = "util-core",
		base = file("util-core"),
		settings = Project.defaultSettings ++ sharedSettings
	).settings(
		name := "util-core",
    libraryDependencies ++= Seq(
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion % "test, provided"
    )
	)

  lazy val websudosUtilHttp = Project(
    id = "util-http",
    base = file("util-http"),
    settings = Project.defaultSettings ++ sharedSettings
  ).settings(
    name := "util-http",
    libraryDependencies ++= Seq(
      "io.netty"        % "netty"                % nettyVersion
    )
  )

  lazy val websudosUtilParsers = Project(
    id = "util-parsers",
    base = file("util-parsers"),
    settings = Project.defaultSettings ++ sharedSettings
  ).settings(
    name := "util-parsers",
    libraryDependencies ++= Seq(
      "commons-validator"       % "commons-validator"               % "1.4.0",
      "joda-time"               %  "joda-time"                      % JodaTimeVersion,
      "org.scalaz"              %% "scalaz-core"                    % ScalazVersion,
      "org.scalatest"           %% "scalatest"                      % ScalaTestVersion % "test, provided"
    )
  ).dependsOn(
    websudosUtilHttp
  )

  lazy val websudosUtilLift = Project(
    id = "util-lift",
    base = file("util-lift"),
    settings = Project.defaultSettings ++ sharedSettings
  ).settings(
    name := "util-lift",
    libraryDependencies ++= Seq(
      "net.liftweb"             %% "lift-webkit"                    % LiftVersion % "compile"
    )
  ).dependsOn(
    websudosUtilParsers
  )

  lazy val websudosUtilAws = Project(
    id = "util-aws",
    base = file("util-aws"),
    settings = Project.defaultSettings ++ sharedSettings
  ).settings(
    name := "util-aws",
    libraryDependencies ++= Seq(
      "com.twitter"             %% "finagle"                           % FinagleVersion,
      "com.twitter"             %% "finagle-core"                      % FinagleVersion exclude("org.slf4j", "slf4j"),
      "com.twitter"             %% "finagle-http"                      % FinagleVersion
    )
  ).dependsOn(
    websudosUtilHttp
  )

  lazy val websudosUtilTesting = Project(
    id = "util-testing",
    base = file("util-testing"),
    settings = Project.defaultSettings ++ sharedSettings
  ).settings(
    name := "util-testing",
    libraryDependencies ++= Seq(
      "com.twitter"                      %% "util-core"                % FinagleVersion,
      "org.scalatest"                    %% "scalatest"                % ScalaTestVersion,
      "org.scalacheck"                   %% "scalacheck"               % "1.11.3"              % "test",
      "org.fluttercode.datafactory"      %  "datafactory"              % "0.8"
    )
  ).dependsOn(
    websudosUtilHttp
  )
}
