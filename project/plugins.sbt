resolvers ++= Seq(
    Classpaths.typesafeReleases,
    "Sonatype snapshots"                                 at "http://oss.sonatype.org/content/repositories/snapshots/",
    "jgit-repo"                                          at "http://download.eclipse.org/jgit/maven",
    "Twitter Repo"                                       at "http://maven.twttr.com/",
    "scct-github-repository"                             at "http://mtkopone.github.com/scct/maven-repo"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.5.0")

addSbtPlugin("com.twitter" % "sbt-package-dist" % "1.1.0")