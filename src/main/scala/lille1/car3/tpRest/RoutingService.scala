package lille1.car3.tpRest

import java.io.{ ByteArrayInputStream, InputStream, OutputStream, File, FileOutputStream, FileInputStream }
import java.io.FileNotFoundException

import scala.util.matching.Regex

import org.apache.commons.net.ftp._

import spray.http._
import spray.http.BodyPart
import spray.http.MediaTypes._
import spray.httpx.unmarshalling.DeserializationError
import spray.json._
import spray.routing._
import directives._
/**
  * Ce trait définit la structure et l'arborescence du routing de l'application sous la forme d'un service HTTP.
  * Il permet de séparer l'instantiation de la route de sa gestion par un acteur. Il implémente les traits HelperHTML,
  * HelperFunction et RejectionHandlerRooting.
  *
  * @author Gouzer Willian
  * @author Philippon Romain
  **/
trait RoutingService extends HttpService with HelperHtml with HelperFunction with RejectionHandlerRooting {
  /**
  * Contient le routing de la passerelle FTP
  */
  val routing =
  (path("") & get) {
    complete(loginForm)
  } ~
  (path("login-action") & post) {
    formFields('server_ip, 'server_port.as[Int], 'login_user, 'mdp_user) { (ip_opt, port_opt, login_opt, mdp_opt) =>
      val connexion = new FTPConnexion(login_opt, mdp_opt, ip_opt, port_opt)
     
      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        setCookie(HttpCookie("ftp_connexion", connexion.info)) {
          complete(loggedInDoneMessage)
        }  
      }
    }  
  } ~
  (path("list") & get) {
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        complete(listNote)
      }
    }
  } ~
  pathPrefix("list" / "html") {
    (pathEnd & get) {
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        var connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login
          try { 
            var liste_files : Array[FTPFile] = connexion.list("")
            complete(HTML_ListResponse("", liste_files))
          } catch {
            case fnfe: FileNotFoundException => complete(HttpResponse(status = StatusCodes.Forbidden, entity = HttpEntity(`text/plain`, "Le dossier que vous voulez lister n'existe pas sur le serveur FTP")))
          }
        }
      }
    } ~
    (path(Segments) & get) { piece_of_route =>
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        var connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login

          piece_of_route match {
            case head :: tail =>
              try { 
                var liste_files : Array[FTPFile] = connexion.list(piece_of_route.mkString("/"))
                complete(HTML_ListResponse("/"+ piece_of_route.mkString("/"), liste_files))
              } catch {
                case fnfe: FileNotFoundException => complete(HttpResponse(status = StatusCodes.Forbidden, entity = HttpEntity(`text/plain`, "Le dossier que vous voulez lister n'existe pas sur le serveur FTP")))
              }
            case List() => complete(HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Le dossier est introuvable</p></body></html>.toString)))
          }
        }
      } 
    }
  } ~
  pathPrefix("list" / "json") {
    (pathEnd & get) {
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        var connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login
          try {
            var liste_files : Array[FTPFile] = connexion.list("")
            complete(JSON_ListResponse("", liste_files))
          } catch {
            case fnfe: FileNotFoundException => complete(HttpResponse(status = StatusCodes.Forbidden, entity = HttpEntity(`text/plain`, "Le dossier que vous voulez lister n'existe pas sur le serveur FTP")))
          }
        }
      }
    } ~
    (path(Segments) & get) { piece_of_route =>
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        var connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login

          piece_of_route match {
            case head :: tail => 
              try { 
                var liste_files : Array[FTPFile] = connexion.list(piece_of_route.mkString("/"))
                  complete(JSON_ListResponse("/"+ piece_of_route.mkString("/"), liste_files))
              } catch {
                case fnfe: FileNotFoundException => complete(HttpResponse(status = StatusCodes.Forbidden, entity = HttpEntity(`text/plain`, "Le dossier que vous voulez lister n'existe pas sur le serveur FTP"))) 
              }
            case List() => complete(HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Le dossier est introuvable</p></body></html>.toString)))
          }
        }
      } 
    }
  } ~
  (path("get" / Segments) & get) { piece_of_route =>
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        connexion.login

        piece_of_route match {
          case head :: tail => 
            val file = File.createTempFile(piece_of_route.mkString("/"), null)
            connexion.download(piece_of_route.mkString("/"), new FileOutputStream(file)) match {
              case true => respondWithMediaType(`application/octet-stream`) { getFromFile(file) }
              case false => complete(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Veuillez retentez l'opération celle-ci vient d'échouer</p></body></html>.toString)))
            }
          case List() => complete(HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Le fichier que vous vouliez télécharger est introuvable</p></body></html>.toString)))
        }
      }
    }
  } ~
  (path("delete" / Segments) & get) { piece_of_route =>
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        connexion.login
    
        piece_of_route match {
          case head :: tail => connexion.delete(piece_of_route.mkString("/")) match {
            case true => redirect("http://localhost/delete/"+ piece_of_route.mkString("/"), StatusCodes.MovedPermanently)
            case false => complete(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Veuillez retentez l'opération celle-ci d'échouer</p></body></html>.toString)))
          }
          case List() => complete(HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Le fichier que vous vouliez supprimer est introuvable</p></body></html>.toString)))
        }
      }
    }
  } ~
  (path("store") & get) {
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        connexion.login
        complete(storeForm)
      }
    }
  } ~
  (path("send") & post) {
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        connexion.login
        entity(as[MultipartFormData]) { formData =>
          val filename = extract(formData.toString, """(filename)(=)([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""", 3)

          formField('file.as[Array[Byte]], 'path.as[String]) { (file, path) =>
            if(path.equals("") || connexion.client.changeWorkingDirectory(path)) {
              val temp_file = File.createTempFile(filename, null)
              val fos = new FileOutputStream(temp_file)
              try { fos.write(file) }
              catch {
                case e : java.io.IOException => complete(HttpResponse(status =StatusCodes.InternalServerError, entity = HttpEntity(`text/plain`, "L'extraction de " + filename +" à échoué")))
                  
              }
              finally { fos.close() }

              connexion.upload(filename, new FileInputStream(temp_file)) match {
                case true => complete(HttpResponse(status = StatusCodes.OK, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Le fichier est bien uploadé</p></body></html>.toString)))
                case false => complete(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Veuillez retentez l'opération celle-ci d'échouer</p></body></html>.toString)))
              }
            }
            else
              complete(HttpResponse(status = StatusCodes.Forbidden, entity = HttpEntity(`text/plain`, "Le dossier de destination " + path +" ne semble pas exister")))
          }
        }
      }
    }
  } ~
  (path("logout") & get)
  {
      deleteCookie("ftp_connexion"){
        complete("Vous êtes déconnecté")
      }
  }
  // fin du routing
}