resolvers ++= Seq(
  Classpaths.typesafeReleases,
  "Sonatype snapshots"                                 at "http://oss.sonatype.org/content/repositories/snapshots/",
  "jgit-repo"                                          at "http://download.eclipse.org/jgit/maven",
  "Twitter Repo"                                       at "http://maven.twttr.com/",
  "scct-github-repository"                             at "http://mtkopone.github.com/scct/maven-repo"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.websudos" %% "sbt-package-dist" % "1.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")
