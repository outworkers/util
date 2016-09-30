resolvers ++= Seq(
  Classpaths.typesafeReleases,
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo,
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "Twitter Repo" at "http://maven.twttr.com/"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.websudos" %% "sbt-package-dist" % "1.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.0.4")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.3.5")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.1.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

if (sys.env.contains("MAVEN_PUBLISH")) {
  addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
} else {
  addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
}