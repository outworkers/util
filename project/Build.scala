import sbt._
import Keys._
import com.twitter.sbt._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._

object util extends Build {

  val nettyVersion = "3.7.0.Final"
	val scalatestVersion = "2.0.M8"
  val finagleVersion = "6.10.0"
  val liftVersion = "2.6-M2"

  val publishSettings : Seq[sbt.Project.Setting[_]] = Seq(
    publishTo := Some("newzly releases" at "http://maven.newzly.com/repository/internal"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true }
  )

	val sharedSettings: Seq[sbt.Project.Setting[_]] = Seq(
		organization := "com.newzly",
		version := "0.0.15",
		scalaVersion := "2.10.3",
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
    newzlyUtilCassandra,
		newzlyUtilCore,
		newzlyUtilFinagle,
    newzlyUtilHttp,
    newzlyUtilLift,
		newzlyUtilTest
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

	lazy val newzlyUtilFinagle = Project(
		id = "util-finagle",
		base = file("util-finagle"),
		settings = Project.defaultSettings ++ VersionManagement.newSettings ++ sharedSettings ++ publishSettings
	).settings(
		name := "util-finagle",
		libraryDependencies ++= Seq(
			"com.twitter"     %% "util-core"           % "6.3.6",
			"org.scalatest"   %% "scalatest"           % scalatestVersion % "provided"  
		)
	)

  lazy val newzlyUtilCassandra = Project(
    id = "util-cassandra",
    base = file("util-cassandra"),
    settings = Project.defaultSettings ++
      assemblySettings ++
      VersionManagement.newSettings ++
      sharedSettings ++ publishSettings
  ).settings(
      name := "util-cassandra",
      jarName in assembly := "cassandra.jar",
      outputPath in assembly := file("cassandra.jar"),
      test in assembly := {},
      fork in run := true,
      assemblyOption in assembly ~= {  _.copy(includeScala = true) } ,
      excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
        cp filter { x =>
          x.data.getName.indexOf("specs2_2.") >= 0 ||
            x.data.getName.indexOf("scalap-2.") >= 0 ||
            x.data.getName.indexOf("scala-compiler.jar") >= 0 ||
            x.data.getName.indexOf("scala-json_") >= 0 ||
            x.data.getName.indexOf("netty-3.2.9") >= 0 ||
            x.data.getName.indexOf("com.twitter") >= 0
        }
      }
    ).settings(
      libraryDependencies ++= Seq(
        "org.scalatest"            %% "scalatest"                         % scalatestVersion,
        "org.cassandraunit"        %  "cassandra-unit"                    % "2.0.2.0"
      )
    ).dependsOn(
      newzlyUtilFinagle
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
		newzlyUtilCore,
		newzlyUtilFinagle
	)

}
