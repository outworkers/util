import bintray.BintrayPlugin.autoImport._
import sbt.Keys._
import sbt.{Credentials, Def, Path, ProjectReference, _}

import scala.util.Properties
import com.typesafe.sbt.pgp.PgpKeys._

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
          realm = "Sonatype OSS Repository Manager",
          host = "oss.sonatype.org",
          userName = System.getenv("maven_user"),
          passwd = System.getenv("maven_password")
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
    version := "0.31.2",
    credentials ++= defaultCredentials
  )

  lazy val pgpPass: Option[Array[Char]] = Properties.envOrNone("pgp_passphrase").map(_.toCharArray)

  lazy val mavenSettings: Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    pgpPassphrase in ThisBuild := {
      if (runningUnderCi && pgpPass.isDefined) {
        println("Running under CI and PGP password specified under settings.")
        println(s"Password longer than five characters: ${pgpPass.exists(_.length > 5)}")
        pgpPass
      } else {
        println("Could not find settings for a PGP passphrase.")
        println(s"pgpPass defined in environemnt: ${pgpPass.isDefined}")
        println(s"Running under CI: $runningUnderCi")
        None
      }
    },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (version.value.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
      } else {
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
      }
    },
    externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = true),
    licenses += ("Outworkers License", url("https://github.com/outworkers/util/blob/develop/LICENSE.txt")),
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true },
    pomExtra :=
      <url>https://github.com/outworkers/util</url>
        <scm>
          <url>git@github.com:outworkers/util.git</url>
          <connection>scm:git:git@github.com:outworkers/util.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alexflav</id>
            <name>Flavian Alexandru</name>
            <url>http://github.com/alexflav23</url>
          </developer>
        </developers>
  ) ++ versionSettings

  val bintraySettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintrayReleaseOnPublish in ThisBuild := true,
    bintrayOrganization := Some("outworkers"),
    bintrayRepository := { if (scalaVersion.value.trim.endsWith("SNAPSHOT")) "oss-snapshots" else "oss-releases" },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  ) ++ versionSettings

  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  def addOnCondition(condition: Boolean, projectReference: ProjectReference): Seq[ProjectReference] =
    if (condition) projectReference :: Nil else Nil

  def jdk8Only(ref: ProjectReference): Seq[ProjectReference] = addOnCondition(isJdk8, ref)

  def effectiveSettings: Seq[Def.Setting[_]] = {
    if (sys.env.contains("MAVEN_PUBLISH")) mavenSettings else bintraySettings
  }
}
