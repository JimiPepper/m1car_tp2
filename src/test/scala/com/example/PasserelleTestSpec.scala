package lille1.car3.tpRest.test

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import spray.routing._
import StatusCodes._
import spray.http.MediaTypes._
import HttpHeaders._
import lille1.car3.tpRest.util.RoutingService

class PasserelleTestSpec extends Specification with Specs2RouteTest with RoutingService {
  def actorRefFactory = system

  "RoutingService" should {
    "vérifier qu'un utilisateur non-loggué ne puisse pas accéder à une section authentifiée de l'application" in {
      Get("/list") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }

      Get("/list/html") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }

      Get("/list/json") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }

      Get("/store") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }

      Post("/send") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }

      Get("/get/un_fichier.txt") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }

      Get("/delete/un_fichier.txt") ~> routing ~> check {
        rejection === MissingCookieRejection("ftp_connexion")
      }
    }

    "renvoie une exception correspondant à une erreur 404 si un utilisateur non-loggué/connecté tente d'accéder à une page qui n'existe pas" in {
      // Utilisateur authentifié
      Get("/willian_est_as_d_emacs") ~> Cookie(HttpCookie("ftp_connexion", "ftptest_test_localhost_21")) ~> routing ~> check {
        rejections must be empty
      }

      // test unitaire : Utilisateur non-authentifié
      Get("/willian_est_as_d_emacs") ~> routing ~> check {
        rejections must be empty
      }
    }

    "transmet une réponse HTML quand l'utilisateur veut un listing en HTML" in {
      Get("/list/html") ~> Cookie(HttpCookie("ftp_connexion", "ftptest_test_localhost_21")) ~> routing ~> check {
        mediaType === `text/html`
      }
    }

    "transmet une réponse JSON quand l'utilisateur veut un listing en JSON" in {
      Get("/list/json") ~> Cookie(HttpCookie("ftp_connexion", "ftptest_test_localhost_21")) ~> routing ~> check {
        mediaType === `application/json`
      }
    }

    "peut se déplacer dans un enfant du répertoire courant, il le peut" in {
      Get("/list/html/toto") ~> Cookie(HttpCookie("ftp_connexion", "ftptest_test_localhost_21")) ~> routing ~> check {
         status === StatusCodes.Forbidden
      }
    }

    "ne pas fournir un fichier qui n'existe pas sur le serveur FTP" in {
      Get("/get/toto_n_existe_pas.txt") ~> Cookie(HttpCookie("ftp_connexion", "ftptest_test_localhost_21")) ~> routing ~> check {
        status === StatusCodes.InternalServerError
      }
    }  

    "déconnecte l'utilisateur quand il se rend sur la page de déconnexion" in {  
      Get("/logout") ~> Cookie(HttpCookie("ftp_connexion", "ftptest_test_localhost_21")) ~> routing ~> check {
        responseAs[String] === "Vous etes deconnecte"
        header[`Set-Cookie`] === Some(`Set-Cookie`(HttpCookie("ftp_connexion", content = "deleted", expires = Some(DateTime.MinValue))))
      }  
    }
  }
}