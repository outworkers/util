resolvers ++= Seq(
  Classpaths.typesafeReleases,
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo,
  "jgit-repo" at "https://download.eclipse.org/jgit/maven",
  "Twitter Repo" at "https://maven.twttr.com/"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.10.0-RC1")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.0.4")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.0")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.2.7")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.6.13")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")

if (sys.env.get("MAVEN_PUBLISH").exists("true" ==)) {
  addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8")
} else {
  addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
}

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.12")
