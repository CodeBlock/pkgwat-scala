import scalariform.formatter.preferences._

name := "pkgwat"

organization := "me.elrod"

version := "1.0.2"

description := "A (rough) port of the pkgwat Fedora Packages API to Scala. "

scalacOptions := Seq("-deprecation", "-unchecked")

scalaVersion := "2.10.2"

licenses := Seq(
  "Apache v2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/CodeBlock/pkgwat-scala"))

pomIncludeRepository := { _ => false }

resolvers ++= Seq(
  "spray" at "http://repo.spray.io/"
)

libraryDependencies   ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
  "io.spray" %% "spray-json" % "1.2.5",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.5" % "test"
)

defaultScalariformSettings ++ Seq(
  ScalariformKeys.preferences := FormattingPreferences().
    setPreference(PreserveDanglingCloseParenthesis, true).
    setPreference(MultilineScaladocCommentsStartOnFirstLine, true).
    setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
)

testOptions in Test += Tests.Argument("-oDS")

site.settings

ghpages.settings

git.remoteRepo := "git@github.com:CodeBlock/pkgwat-scala.git"

site.includeScaladoc()

seq(ScctPlugin.instrumentSettings : _*)

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>git@github.com:CodeBlock/pkgwat-scala.git</url>
    <connection>scm:git:git@github.com:CodeBlock/pkgwat-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>relrod</id>
      <name>Ricky Elrod</name>
      <url>http://elrod.me/</url>
    </developer>
  </developers>
)

