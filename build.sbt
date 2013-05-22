name := "pkgwat-scala"

organization := "org.fedoraproject"

version := "1.0.0"

description := "A (rough) port of the pkgwat Fedora Packages API to Scala. "

scalacOptions := Seq("-deprecation", "-unchecked")

scalaVersion := "2.10.1"

licenses := Seq(
  "Apache v2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

resolvers ++= Seq(
)

libraryDependencies   ++= Seq(
  "net.liftweb" % "lift-json_2.10" % "2.5-M4"
)
