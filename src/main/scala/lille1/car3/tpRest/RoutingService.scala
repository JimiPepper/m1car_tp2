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
import directives._

// L'acteur qui se charge d'éxécuter les différentes actions qu'il reçoit en fonction du routing qui lui est transmit
class RoutingActor extends Actor with RoutingService with HelperHtml {

  def actorRefFactory = context

  def receive = runRoute(routing)
}

// Cette structure trait dissocie la création du routing de sa gestion
trait RoutingService extends HttpService with HelperHtml with HelperFunction with RejectionHandlerRooting {
  val routing =
  (path("") & get) {
    complete(loginForm)
  } ~
  (path("login-action") & post) {
    formFields('server_ip, 'server_port.as[Int], 'login_user, 'mdp_user) { (ip_opt, port_opt, login_opt, mdp_opt) =>
      val connexion = new FtpConnexion(login_opt, mdp_opt, ip_opt, port_opt)
     
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
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

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
        var connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login
          complete(HTML_ListResponse("", connexion.list("")))
        }
      }
    } ~
    (path(Segments) & get) { piece_of_route =>
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        var connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login

          piece_of_route match {
            case head :: tail => complete(HTML_ListResponse("/"+ piece_of_route.mkString("/"), connexion.list(piece_of_route.mkString("/"))))
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
        var connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login
          complete(JSON_ListResponse(connexion.list("")))
        }
      }
    } ~
    (path(Segments) & get) { piece_of_route =>
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        var connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login

          piece_of_route match {
            case head :: tail => complete(JSON_ListResponse(connexion.list(piece_of_route.mkString("/"))))
            case List() => complete(HttpResponse(status = StatusCodes.NoContent, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Le dossier est introuvable</p></body></html>.toString)))
          }
        }
      } 
    }
  } ~
  (path("get" / Segments) & get) { piece_of_route =>
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

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
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

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
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

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
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        connexion.login
        entity(as[MultipartFormData]) { formData =>
          val filename = extract(formData.toString, """(filename)(=)([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""", 3)

          formField('file.as[Array[Byte]]) { file =>
            val temp_file = File.createTempFile(filename, null)
            val fos = new FileOutputStream(temp_file)
            try { fos.write(file) }
            catch {
              case e : java.io.IOException =>
                
            }
            finally { fos.close() }

            connexion.upload(filename, new FileInputStream(temp_file)) match {
              case true => complete(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Veuillez retentez l'opération celle-ci d'échouer</p></body></html>.toString)))
              case false => complete(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(`text/html`, <html><head><title></title></head><body><p>Veuillez retentez l'opération celle-ci d'échouer</p></body></html>.toString)))
            }
          }
        }
      }
    }
  }
  // fin du routing
}
// fin trait RoutingService

/*
      /*
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login
          if (path == "" || path == "default") {
            connexion.cwd("")
            complete(HTML_ListResponse("", connexion.list("")))
          } else {
            val long_path = (path.split("/"))(path.split("/").length-1)
            connexion.cwd(long_path)
            println("LONG_PATH: " +long_path)
            complete(HTML_ListResponse(long_path, connexion.list("")))
          }
        }
      }
      */
    (path("json" / """.*""".r) & get) { path =>
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          connexion.login
          complete(JSON_ListResponse(connexion.list("")))
        }
      }
    } 
    */