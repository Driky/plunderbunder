name := """plunderbunder"""

version := "0.1-ALPHA"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.typesafe.akka" %% "akka-contrib" % "2.3.9",
  "org.webjars" % "react" % "0.12.1",
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "typeaheadjs" % "0.10.5-1",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "requirejs" % "2.1.15"
)

jacoco.settings