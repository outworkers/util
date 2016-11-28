import bintray.BintrayPlugin.autoImport._
import sbt.Keys._
import sbt.{Credentials, Def, Path, ProjectReference, _}

object Publishing {

  def runningUnderCi: Boolean = sys.env.contains("CI") || sys.env.contains("TRAVIS")

  lazy val defaultCredentials: Seq[Credentials] = {
    if (!runningUnderCi) {
      Seq(
        Credentials(Path.userHome / ".bintray" / ".credentials"),
        Credentials(Path.userHome / ".ivy2" / ".credentials")
      )
    } else {
      Seq(
        Credentials(
          realm = "Bintray",
          host = "dl.bintray.com",
          userName = System.getenv("bintray_user"),
          passwd = System.getenv("bintray_password")
        ),
        Credentials(
          realm = "Bintray API Realm",
          host = "api.bintray.com",
          userName = System.getenv("bintray_user"),
          passwd = System.getenv("bintray_password")
        )
      )
    }
  }

  val versionSettings = Seq(
    version := "0.24.0",
    credentials ++= defaultCredentials
  )

  val bintraySettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintrayReleaseOnPublish in ThisBuild := true,
    bintrayOrganization := Some("outworkers"),
    bintrayRepository <<= scalaVersion.apply {
      v => if (v.trim.endsWith("SNAPSHOT")) "oss-snapshots" else "oss-releases"
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  ) ++ versionSettings

  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  def addOnCondition(condition: Boolean, projectReference: ProjectReference): Seq[ProjectReference] =
    if (condition) projectReference :: Nil else Nil

  def jdk8Only(ref: ProjectReference): Seq[ProjectReference] = addOnCondition(isJdk8, ref)

  def effectiveSettings: Seq[Def.Setting[_]] = bintraySettings
}
