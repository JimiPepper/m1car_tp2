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
  var current_connexion : Option[FtpConnexion] = None

  val default_route =
    (path("") & get) {
      respondWithMediaType(`text/html`) {
        complete(loginForm)
      }
    } ~
  pathPrefix("list") {
    pathEnd { complete("Use list/html or list/json") } ~
    path("html") {
      val list = listDirectoryContents("pics/")
      respondWithMediaType(`text/html`) {
        list
      }
      /**
        * TODO : En attente de l'authentification & Gestion couche FTP
        * complete(HTML_ListResponse(client.listFiles)
        */
    } ~
    path("json") {
      complete("JSON time")
      /**
        * TODO : En attente de l'authentification & Gestion couche FTP
        * complete(JSON_ListResponse(client.listFiles)
        */
    }
  } ~
    (path("get" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>
    val file = File.createTempFile(filename, null)

    // TODO better handling for connexions ! Does it need a proper authentification ?
    val t : Option[FtpConnexion] = Some(new FtpConnexion)
    val Some(cl) = t
    cl.connect("localhost", 21)
    cl.login("ftptest", "test")

    t match {
      case Some(client) =>
        if (client.download(filename, new FileOutputStream(file))) {
          respondWithMediaType(`application/octet-stream`) {
            getFromFile(file)
          }
        } else {
          complete{"Failed to download " + filename}
        }
      case None => complete{"Failed to retrieve FTP connexion, please relog"}
    }

  } ~
    (path("delete" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { filename =>

    // TODO better handling for connexions ! Does it need a proper authentification ?
    val t : Option[FtpConnexion] = Some(new FtpConnexion)
    val Some(cl) = t
    cl.connect("localhost", 21)
    cl.login("ftptest", "test")

    t match {

      case Some(client) =>
        // TODO ftpPath + filename
        if (client.delete(filename)) {
          complete{filename + " sucessfully deleted!"}
        } else {
          complete{"Failed to delete " + filename}
        }
      case None => complete{"Failed to retrieve FTP connexion, please relog"}
    }

    complete("Delete, done")
    // }
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

          // TODO better handling for connexions ! Does it need a proper authentification ?
          val t : Option[FtpConnexion] = Some(new FtpConnexion)
          val Some(cl) = t
          cl.connect("localhost", 21)
          cl.login("ftptest", "test")

          t match {
            case Some(client) =>
              // TODO 1st arg: ftpPath + filename
              // TODO fos: current dir + filename

              if (client.upload(filename, new FileInputStream(temp_file))) {
                complete{filename + " sucessfully uploaded!"}
              } else {
                complete{"Failed to upload " + filename}
              }
            case None => complete{"Failed to retrieve FTP connexion, please relog"}
          }

          complete { filename + " Successfully uploaded" }
        }
      }
    }
  } ~
    (path("loginAction") & post) {
    formFields('server_ip.?, 'server_port.as[Int].?, 'login_user.?, 'mdp_user.?) {
      (ip_opt, port_opt, login_opt, mdp_opt) =>

      // TODO gerer l'ip vide
      val ip = ip_opt match {
        case Some(value) if value != "" => value
        case _ => complete("Failed to find the IP address"); "None"
      }

      val port = port_opt match {
        case Some(value) if value != "" => value
        case _ => complete("Failed to find the port"); 0
      }

      val login = login_opt match {
        case Some(value) if value != "" => value
        case _ => "anonymous"
      }

      val mdp = mdp_opt match {
        case Some(value) if value != "" => value
        case _ => "mdp"
      }

      val current_connexion = new FtpConnexion
      try {
        current_connexion.connect(ip, port)
      } catch {
        case e: org.apache.commons.net.ftp.FTPConnectionClosedException =>
          complete("FAILED to connect to " + ip + ":" + port + "!")
        case f: java.io.IOException =>
          complete("FAILED to connect to " + ip + ":" + port + "!")
      }

      if (! current_connexion.login(login, mdp)) {
        current_connexion.disconnect; complete("FAILED to login!")
      }
      complete {
        "Connexion to " +ip+ ":" +port+" with " + "["+login+"]:["+mdp+"]" + " => successful"
        // HttpResponse(
        //   status = redirectionType,
        //   headers = Location(uri) :: Nil,
        //   entity = redirectionType.htmlTemplate match {
        //     case ""       ⇒ HttpEntity.Empty
        //     case template ⇒ HttpEntity(`text/html`, template format uri)
        //   })
      }

    }
  }
  // fin de la route !

}
