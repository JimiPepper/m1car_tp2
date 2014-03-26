package lille1.car3.tpRest

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Boot extends App {
	/**
  * Objet singleton qui est le "Main" du programme. Il instancie un système d'acteurs,
  * qui géèe les requêtes HTTP arrivant sur le port 8080 en fonction de la route défini au
  * sein des acteurs.
  **/

  // Création d'un acteur akka pour gérer le serveur Spray
  implicit val system = ActorSystem()

  // Démarrage du serveur sur l'acteur RoutingActor qui se charge de récupérer les réponses HTTP
  val handler = system.actorOf(Props[RoutingActor], name = "handler")

  // Lancement d'un nouveau serveur HTTP sur le port 8080 sur l'acteur RoutingActor
  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
}
