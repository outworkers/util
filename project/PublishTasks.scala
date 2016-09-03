import bintray.BintrayPlugin.autoImport._
import sbt.Keys._
import sbt.{Credentials, Def, Path, ProjectReference, _}

object Publishing {
  val mavenSettings : Seq[Def.Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishTo <<= version.apply {
      v =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "content/repositories/snapshots")
        }
        else {
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
        }
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

  val bintraySettings : Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintrayReleaseOnPublish in ThisBuild := true,
    bintrayOrganization := Some("websudos"),
    bintrayRepository := "oss-releases",
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )

  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  def addOnCondition(condition: Boolean, projectReference: ProjectReference): Seq[ProjectReference] =
    if (condition) projectReference :: Nil else Nil

  def jdk8Only(ref: ProjectReference): Seq[ProjectReference] = addOnCondition(isJdk8, ref)

  def effectiveSettings: Seq[Def.Setting[_]] = {
    if (sys.env.contains("MAVEN_PUBLISH")) mavenSettings else bintraySettings
  }
}

object TravisEnv {
  val RunningUnderCi = Option(System.getenv("CI")).isDefined || Option(System.getenv("TRAVIS")).isDefined
  lazy val TravisScala211 = Option(System.getenv("TRAVIS_SCALA_VERSION")).exists(_.contains("2.11"))

  lazy val defaultCredentials: Seq[Credentials] = {
    if (!RunningUnderCi) {
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
}
