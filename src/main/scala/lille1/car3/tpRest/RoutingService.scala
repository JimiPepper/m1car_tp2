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
          //complete(loggedInDoneMessage)
          pathEnd {
            redirect("/list/html/default", StatusCodes.SeeOther) // TemporaryRedirect
          }
        }
      }
    } ~
  pathPrefix("list") {
    (pathEnd & get) {
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
          complete(listNote)
        }
      }
    } ~
      (path("html" / """.*""".r) & get) { path =>
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
    } ~
      (path("json" / """.+""".r) & get) { path =>
      cookie("ftp_connexion") { cookie_ftp =>
        var tab : Array[String] = cookie_ftp.content.split('_')
        val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

        connexion.connect
        validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités"){
          connexion.login
          complete(JSON_ListResponse(connexion.list("")))
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
        connexion.login
        connexion.download(filename, new FileOutputStream(file)) match {
          case true =>
            respondWithMediaType(`application/octet-stream`) { getFromFile(file) }
          case false => complete("Cannot download " + filename)
        }
      }
    }

  } ~
    (path("delete" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>
    cookie("ftp_connexion") { cookie_ftp =>
      var tab : Array[String] = cookie_ftp.content.split('_')
      val connexion = new FtpConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

      connexion.connect
      validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
        connexion.login
        connexion.delete(filename) match {
          case true => complete("true")
          case false => complete("false")
        }
      }
    }

  } ~
  pathPrefix("store") {
    (pathEnd & get) {
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
      (path("file") & post) {
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
                  println("Failed to retrieve file")
                  complete("Failed to retrieve file")
              }
              finally { fos.close() }

              connexion.upload(filename, new FileInputStream(temp_file)) match {
                case true => complete(storeDoneMessage)
                case false => complete("Failed to store " + filename)
              }
            }
          }
        }
      }
    }
  }
  // fin du routing
}
// fin trait RoutingService
