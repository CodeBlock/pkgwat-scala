resolvers ++= Seq(
    "sonatype-oss-repo" at "https://oss.sonatype.org/content/groups/public/",
    "jgit-repo" at "http://download.eclipse.org/jgit/maven"
)

resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.scct" % "sbt-scct" % "0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")
