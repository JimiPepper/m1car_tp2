package lille1.car3.tpRest

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import spray.routing._
import StatusCodes._
import spray.http.MediaTypes._
import HttpHeaders._

/*
 * Liste des tests à implémenter :
 *       -> Vérifier qu'un utilisateur non-loggué ne puisse pas accéder l'application (renvoi d'une exception) +
 *       -> Si un utilisateur ne fait rien pendant x secondes/minutes, le déconnecter
 *       -> Si un utilisateur non-loggué/connecté tente d'accéder à une page qui n'existe pas (william_est_un_as_d_emacs) renvoyer une exception +
 *       -> Si un utilisateur tente de RETR un fichier qui n'existe pas renvoyer une exception
 *       -> Si un utilisateur veut se déplacer dans un enfant du répertoire courant, il le peut
 *       -> Si l'utilisateur veut PUT un fichier sur le serveur sans passer par storeFile, renvoyer une exception
 *       -> Si l'utilisateur souhaite qu'on lui renvoie du html, on lui envoie du html +
 *       -> Si l'utilisateur renvoie du JSON, on lui envoie du JSON +
 *       -> Quand l'utilisateur se déconnecte, il est bien déconnecté du serveur FTP
 */

class MyServiceSpec extends Specification with Specs2RouteTest with RoutingService {
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
  }
}