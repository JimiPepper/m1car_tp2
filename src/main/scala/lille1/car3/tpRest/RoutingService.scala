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

  def receive = runRoute(default_route)
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

  val default_route =
    (path("") & get) {
      val info = new FtpConnexion("ftptest", "test", "localhost", 21).info;
      setCookie(HttpCookie("ftp_connexion", content = info)){
        complete(info)}

      // respondWithMediaType(`text/html`) {
      //   complete(loginForm)
      // }
    } ~
  pathPrefix("list") {
    pathEnd { complete("Use list/html or list/json") } ~
    path("html") {
      cookie("ftp_connection") {
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
        
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités"){
            complete(HTML_ListResponse(connexion.listFiles))      
        }
      }
      } ~
      path("json") {
        complete("JSON time")
        /**
          * TODO : En attente de l'authentification & Gestion couche FTP
          * complete(JSON_ListResponse(client.listFiles)
          */
      }
    } ~
    path("json") {
      cookie("ftp_connection") {
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)
        
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités"){
            complete(JSON_ListResponse(connexion.listFiles))      
        }
      }
    }
  } ~
    (path("get" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>
    val file = File.createTempFile(filename, null)
    // TODO better handling for connexions ! Does it need a proper authentification ?
    // if (client.download(filename, new FileOutputStream(file))) {
    //   respondWithMediaType(`application/octet-stream`) {
    //     getFromFile(file)
    //   }
    // }
    complete("d")
  } ~
    (path("delete" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>
    // TODO better handling for connexions ! Does it need a proper authentification ?
    // if (client.delete(filename)) {
    // }
    complete("Delete, done")

  } ~
  pathPrefix("store") {
    get { complete(storeForm) } ~
      (path("file") & post) {
      entity(as[MultipartFormData]) { formData =>
        val filename = extract(
          formData.toString,
          """(filename)(=)([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""",
          3)

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
          complete("d")
          // TODO better handling for connexions ! Does it need a proper authentification ?
          // if (client.upload(filename, new FileInputStream(temp_file))) {
          // }
        }
      }
    }
  } ~
    (path("loginAction") & post) {
    formFields('server_ip.?, 'server_port.as[Int].?, 'login_user.?, 'mdp_user.?) {
      (ip_opt, port_opt, login_opt, mdp_opt) =>

      val connexion = new FtpConnexion(login_user, mdp_user, server_ip, port_opt)
      /*
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
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          setCookie("fto_connexion", connexion.info) { 
            complete(HttpResponse(
                status = redirectionType,
                entity = HttpEntity(
                <html>
                <head>
                    <title></title>
                </head>.toString
                )
            )
          }
      }
      // fin de la route !

    }
  }
}
