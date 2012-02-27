package nl.ing.rbb.channels.mobile.server.fake.actors

import akka.actor.ActorRef
import cc.spray._
import http.{HttpHeaders,HttpResponse,StatusCodes}
import typeconversion.SprayJsonSupport
import utils._

import org.scala_tools.time.Imports._


class RestService() extends Directives with SprayJsonSupport {

  val restService = {
    // Debugging: /ping -> pong
    path("ping") {
      get { _.complete("pong") }
    } ~
    path("") {
      get { _.redirect("index.html#Scoreboard", StatusCodes.MovedPermanently) }
    } ~
    getFromResourceDirectory("www")
  }
}
