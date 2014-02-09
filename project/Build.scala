import sbt._
import Keys._


object util extends Build {

	val finagleVersion = "6.10.0"

	lazy val newzlyUtil = Project(
		id = "newzly-util",
		base = file("."),
		settings = Project.defaultSettings
	).aggregate(
		newzlyUtilCore,
		newzlyUtilFinagle,
		newzlyUtilLift
	)

	lazy val newzlyUtilCore = Project(
		id = "newzly-util-core",
		base = file("newzly-util-core"),
		settings = Project.defaultSettings
	).settings(
		name := "newzly-util-core"
	)

	lazy val newzlyUtilFinagle = Project(
		id = "newzly-util-finagle",
		base = file("newzly-util-finagle"),
		settings = Project.defaultSettings
	).settings(
		name := "newzly-util-finagle",
		libraryDependencies ++= Seq(
			"com.twitter"     %%  "util-core"          % "6.3.6"
		)
	).dependsOn(
		newzlyUtilCore
	)
}