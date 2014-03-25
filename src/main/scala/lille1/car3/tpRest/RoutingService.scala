package lille1.car3.tpRest

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

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class RoutingActor extends Actor with RoutingService with HelperHtml {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling

  def receive = runRoute(routing)
}

// this trait defines our service behavior independently from the service actor
trait RoutingService extends HttpService with HelperHtml with HelperFunction {
  implicit val myRejectionHandler = RejectionHandler {
    case MissingCookieRejection(cookieName) :: _ =>
      complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Vous devez d'abord <a href=\"http://localhost:8080\">vous authentifiez</a> pour utiliser ce service</p>")))
    case ValidationRejection(message, cause) :: _ =>
      complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Votre session a expiré, veuillez <a href=\"http://localhost:8080\">vous reconnectez</a>")))
    case UnacceptedResponseContentTypeRejection(param) :: _ =>
      complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Erreur 404, la ressource recherchée n'existe pas")))
  }

  val routing =
  (path("") & get) {
      complete(loginForm)
  } ~
  (path("login-action") & post) {
    formFields('server_ip, 'server_port.as[Int], 'login_user, 'mdp_user) { (ip_opt, port_opt, login_opt, mdp_opt) =>
      val connexion = new FtpConnexion(login_opt, mdp_opt, ip_opt, port_opt)
      /* NE PAS SUPPRIMER TANT QUE JE NE LE SUPPRIME PAS MOI-MÊME (Romain)
      try {
        current_connexion.connect(ip, port)
      } catch {
        // la connexion est restée fermée
        case e: org.apache.commons.net.ftp.FTPConnectionClosedException =>
          complete("FAILED to connect to " + ip + ":" + port + "!")
        case f: java.io.IOException =>
            // erreur sur les sockets
          complete("FAILED to connect to " + ip + ":" + port + "!")
      }

      if (! current_connexion.login(login, mdp)) {
        current_connexion.disconnect; 
        complete("FAILED to login!")
      }
      */
      connexion.connect
      connexion.login
      // TODO : Faire le test à la main
      setCookie(HttpCookie("ftp_connexion", connexion.info)) {
        complete(loggedInDoneMessage)
      }
    }  
  } ~ 
  pathPrefix("list") {
    (pathEnd & get) { 
      cookie("ftp_connection") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          complete(listNote)    
        }
      }
    } ~
    (path("html") & get) {
      cookie("ftp_connection") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
        
        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités"){
            complete(HTML_ListResponse(connexion.list(""))) // TODO : Ajouter le chemin du dossier à lister    
        }
      } 
    } ~
    (path("json") & get) {
      cookie("ftp_connection") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
        
        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités"){
          complete(JSON_ListResponse(connexion.list(""))) // TODO : Ajouter le chemin du dossier à lister   
        }
      }
    }   
  } ~
  (path("get" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>
    val file = File.createTempFile(filename, null)

    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
      
      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        /*
        * TODO : Terminer le get 
        *  if (client.download(filename, new FileOutputStream(file))) {
        *   respondWithMediaType(`application/octet-stream`) {
        *     getFromFile(file)
        *   }
        *  }
        */
        complete("Du vide")
      } 
    }
  } ~
  (path("delete" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>
    cookie("ftp_connection") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
      
      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        /*
        * TODO : Terminer le delete
        * if(connexion.delete(filename)) 
        *   // Du code manquant ?
        *
        * complete(deleteDoneMessage)
        */ 
        complete("Du vide") // complete temporaire   
      }
    }
  } ~
  pathPrefix("store") {
    (pathEnd & get) { 
      cookie("ftp_connection") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
        
        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          complete(storeForm)
        }
      }  
    } ~
    (path("file") & post) {
      cookie("ftp_connection") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
        
        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          entity(as[MultipartFormData]) { formData =>
            val filename = extract(formData.toString, """(filename)(=)([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""", 3)

            formField('file.as[Array[Byte]]) { file =>
              val temp_file = File.createTempFile(filename, null)
              val fos = new FileOutputStream(temp_file)
              try { fos.write(file) }
              catch {
                case e : java.io.IOException =>
                  println("Failed to retrieve file")
                  complete("Failed to retrieve file")
              }
              finally { fos.close() }
              
              complete(storeDoneMessage)
            }  
          }
        }
      }
    } 
  }
  // fin du routing
}