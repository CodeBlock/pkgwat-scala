import scalariform.formatter.preferences._

name := "pkgwat-scala"

organization := "org.fedoraproject"

version := "1.0.0"

description := "A (rough) port of the pkgwat Fedora Packages API to Scala. "

scalacOptions := Seq("-deprecation", "-unchecked")

scalaVersion := "2.10.1"

licenses := Seq(
  "Apache v2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

resolvers ++= Seq(
  //"spray" at "http://repo.spray.io/",
  "relrod @ FedoraPeople" at "http://codeblock.fedorapeople.org/maven/"
)

libraryDependencies   ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  //"io.spray" %% "spray-json" % "1.2.4",
  "org.fedorapeople.codeblock" %% "spray-json" % "1.2.5-SNAPSHOT",
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
