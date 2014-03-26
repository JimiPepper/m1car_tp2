package lille1.car3.tpRest

import spray.routing._
import spray.http._
import StatusCodes._
import Directives._
import MediaTypes._

/**
  * Contient les redirections personnalisées servant à la gestion des cas d'erreurs lorsqu'une route renvoie une Rejection
  *
  * @author Gouzer Willian
  * @author Philippon Romain
  **/
trait RejectionHandlerRooting
{
	/**
	* Surcharge la gestion des rejections de Spray en modifiant le contenu des réponses HTTP symbolisé par la classe HttpResponse de Spray
	**/
	implicit val myRejectionHandler = RejectionHandler {
    case MissingCookieRejection(cookieName) :: _ =>
      complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Vous devez d'abord <a href=\"http://localhost:8080\">vous authentifiez</a> pour utiliser ce service</p>")))
    case ValidationRejection(message, cause) :: _ =>
      complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Votre session a expiré, veuillez <a href=\"http://localhost:8080\">vous reconnectez</a>")))
    case UnacceptedResponseContentTypeRejection(param) :: _ =>
      complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Erreur 404, la ressource recherchée n'existe pas")))
  }
} 