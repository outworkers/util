resolvers ++= Seq(
    Classpaths.typesafeReleases,
    "Sonatype snapshots"                                 at "http://oss.sonatype.org/content/repositories/snapshots/",
    "jgit-repo"                                          at "http://download.eclipse.org/jgit/maven",
    "Twitter Repo"                                       at "http://maven.twttr.com/",
    "scct-github-repository"                             at "http://mtkopone.github.com/scct/maven-repo"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "0.98.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.5.0")
