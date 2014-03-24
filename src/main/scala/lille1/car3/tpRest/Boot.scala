package lille1.car3.tpRest

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {
  // we need an ActorSystem to host our application in
  // implicit val system = ActorSystem("on-spray-can")
  implicit val system = ActorSystem()

  // create and start our service actor
  val handler = system.actorOf(Props[RoutingService], name = "handler")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
}
