package nl.ing.rbb.channels.mobile.server.fake.actors

import akka.config.Supervision._
import akka.actor.Supervisor
import akka.actor.Actor._
import cc.spray._

class Boot {

  val mainModule = new RestService()
  val restService = actorOf(new HttpService(mainModule.restService))
  val rootService = actorOf(new RootService(restService))

  // Start all actors that need supervision, including the root service actor.
  Supervisor(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      List(
        Supervise(rootService, Permanent),
        Supervise(restService, Permanent)
      )
    )
  )
}
