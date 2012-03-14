name := "rhinos"

version := "0.3.0-SNAPSHOT"

organization := "com.scalapeno"

scalaVersion := "2.9.1"

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.cc"
)

libraryDependencies ++= Seq(
  "cc.spray" %% "spray-json" % "1.1.1",
  "org.mozilla" % "rhino" % "1.7R3",
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "provided",
  "org.specs2" %% "specs2" % "1.8.2" % "test"
)
