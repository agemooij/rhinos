name := "rhinos"

version := "0.1"

organization := "com.scalapeno"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.cc"
)

libraryDependencies ++= Seq(
  "cc.spray" %% "spray-json" % "1.1.0" % "compile" withSources(),
  "org.mozilla" % "rhino" % "1.7R3",
  "org.specs2" %% "specs2" % "1.8.2" % "test"
)
