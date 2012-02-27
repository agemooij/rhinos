name := "mobile-fake-server"

version := "1.0"

organization := "nl.ing.rbb.channels.mobile"

scalaVersion := "2.9.1"

seq(webSettings :_*)

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.cc",
  "Akka Repository" at "http://akka.io/repository"
)

libraryDependencies ++= Seq(
  "cc.spray" % "spray-server" % "0.8.0" % "compile" withSources(),
  "cc.spray" %% "spray-json" % "1.1.0" % "compile" withSources(),
  "org.scala-tools.time" %% "time" % "0.5" % "compile",
  "se.scalablesolutions.akka" % "akka-actor" % "1.2",
  "se.scalablesolutions.akka" % "akka-http" % "1.2",
  "org.mozilla" % "rhino" % "1.7R3",
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.specs2" %% "specs2" % "1.8.2" % "test"
)
