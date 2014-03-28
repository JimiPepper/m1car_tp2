package lille1.car3.tpRest.actor

import lille1.car3.tpRest.util.RoutingService

import java.io.{ ByteArrayInputStream, InputStream, OutputStream, File, FileOutputStream, FileInputStream }

import scala.util.matching.Regex

import org.apache.commons.net.ftp._

import akka.actor.{ Props, Actor }

import spray.http._
import spray.http.BodyPart
import spray.http.MediaTypes._
import spray.httpx.unmarshalling.DeserializationError
import spray.json._
import spray.routing._
import directives._

/**
  * Instancie un nouvel acteur Akka correspondant à un contexte d'exécution. Cet acteur redirigera les différentes 
  * requêtes HTTP en fonction du routing qui lui est transmit par le Trait RoutingService
  *
  * @author Gouzer Willian
  * @author Philippon Romain
  **/
class RoutingActor extends Actor with RoutingService {

  /**
  * Contient l'acteur généré par le serveur Spray via la factory des Acteurs lors du lancement de l'application
  */
  def actorRefFactory = context

  /**
  * Défini le routing que doit gérer l'instance RoutingActor pour exécuter l'application
  **/
  def receive = runRoute(routing)
}