package lille1.car3.tpRest

import java.io.{ ByteArrayInputStream, InputStream, OutputStream, FileOutputStream, FileInputStream }

import scala.util.matching.Regex

import org.apache.commons.net.ftp._

import akka.actor.{ Props, Actor }

import spray.http._
import spray.http.MediaTypes._
import spray.httpx.unmarshalling.DeserializationError
import spray.routing._
import spray.http.BodyPart
import spray.json._

import DefaultJsonProtocol._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class RoutingService extends Actor with myRoutingService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling

  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait myRoutingService extends HttpService {
  var curConnexion : FtpConnexion = null

  val myRoute =
    (path("") & get) { // `text/xml` by default, override to be sure
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
    } ~
    path("json") {
      complete("JSON time")
    }
  } ~
    (pathPrefix("get" / """([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""".r) & get) { str =>
    respondWithMediaType(`application/octet-stream`) {
      getFromFile("pics/" + str)
    }
  } ~
    (pathPrefix("delete") & get) {
    entity(as[MultipartFormData]) { formData =>
      val filename = extract(
        formData.toString,
        """(filename)(=)([-_.a-zA-Z0-9]+[.]+[a-zA-Z0-9]{2,})""",
        3)
      // TODO delete
      complete("")
    }
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
          val fos : FileOutputStream = new FileOutputStream(filename)
          try {
            fos.write(file)
          } finally {
            fos.close
          }
          complete { filename + "Successfully uploaded" }
        }
      }
    }
  } ~
    (pathPrefix("loginAction") & post) {
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

      curConnexion = new FtpConnexion
      try {
        curConnexion.connect(ip, port)
      } catch {
        case e: org.apache.commons.net.ftp.FTPConnectionClosedException =>
          complete("FAILED to connect to " + ip + ":" + port + "!")
      }

      if (! curConnexion.login(login, mdp)) {
        curConnexion.disconnect; complete("FAILED to login!")
      }
      else {
        complete("Connexion to " +ip+ ":" +port+" with " + "["+login+"]:["+mdp+"]" + " => successful")
      }
    }
  }
  // fin de la route !

  def extract(text: String, regex: String, idx: Int) : String = {
    val Some(reg) = regex.r.findFirstMatchIn(text)
    reg.group(idx)
  }

  /* OVERRIDE OBJECTS */
  object MyJsonProtocol extends DefaultJsonProtocol {
    // on ajoute le type List[Map, Map] au types de bases supporté par Spray pouvant être sérialisé au format JSON
    implicit object MyJsonFormat extends RootJsonFormat[List[Map[String, String]]] {
      def returnJSONElement() : JsObject = JsObject("filename" -> JsString("Ta mere encule"), "chiffre" -> JsNumber(10))
      def write(list: List[Map[String, String]]) = {
        JsArray(list.map(elem => returnJSONElement))
      }

      def read(value: JsValue): List[Map[String, String]] = value match {
        case JsArray(content) => {
          var responseJSON : List[Map[String, String]] = List[Map[String, String]]()
          var map : Map[String, String] = Map[String, String]()

          for(JSON_elem <- content) {
            JSON_elem.asJsObject.getFields("filename", "chiffre") match {
              case Seq(JsString(filename), JsNumber(chiffre)) => map + ("filename" -> filename)
              case _ => deserializationError("Erreur de formatage ~ le JSON attendu ne correspnd au format List[Map[String, String]]")
            }

            responseJSON = map :: responseJSON
            /* ... */
            map = Map[String, String]()
          }

          responseJSON
        }
        case _ => deserializationError("List[Map[String, String]] attendu")
      }
    }
  }

  import MyJsonProtocol._

  /* HELPERS FUNCTIONS */
  def JSON_ListResponse(files: Array[FTPFile]) : HttpResponse = {
    var responseJSON : List[Map[String, String]] = List[Map[String, String]]()
    var map : Map[String, String] = Map[String, String]()

    for(f <- files) {
      map + ("filename" -> f.getName())
      responseJSON = map :: responseJSON
      /* ... */
      map = Map[String, String]()
    }

    HttpResponse(
      status = 200,
      entity = HttpEntity(`text/html`, responseJSON.toJson.toString)
    )
  }

  def HTML_ListResponse(files: Array[FTPFile]) : HttpResponse = {
    var responseHTML = new String("<html><head><title>Commande LIST - HTML</title></head><body><h1>Commande LIST FTP - Version HTML</h1><ul>")
    for(f <- files) responseHTML += "<li>"+ f.getName() +"</li>"
    responseHTML += "</ul></body></html>"

    HttpResponse(
      status = 200,
      entity = HttpEntity(`text/html`, responseHTML)
    )
  }

  lazy val loginForm = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <head>
        <title>Passerelle REST</title>
        </head>
        <body>
        <h1>Connexion - FTP</h1>

      <form name="loginForm" method="post" action="loginAction">
        <div>
        <h3>Serveur FTP</h3>
        <label for="server_ip">IP : </label><input type="text" name="server_ip"/>
        <label for="server_port">Port : </label><input type="text" name="server_port"/>
        </div>

      <div>
        <h3>Utilisateur</h3>
        <label for="login">Identifiant : </label><input type="text" name="login_user"/><br/>
        <label for="mdp">Mot de passe : </label><input type="password" name="mdp_user"/><br/>
        <input type="submit" value="Se connecter" />
        </div>
        </form>
        </body>
        </html>.toString
    )
  )

  lazy val storeForm = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <head>
        <title>Déposer un fichier</title>
        </head>
        <body>
        <h1>Commande STORE - Déposer votre fichier sur le serveur FTP</h1>
        <form name="storeForm" method="post" enctype="multipart/form-data" action="store/file">
        <input name="file" type="file" />
        <input type="submit" value="Déposer" />
        </form>
        </body>
        </html>.toString
    )
  )

  lazy val listNote = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <head>
        <title>Note LIST</title>
        </head>
        <body>
        <h1>Utilisation de LIST</h1>
        <p>
        Pour effectuer correctement la commande LIST, veuillez utiliser :
          <ul>
        <li>L'URL <a href="list/html">list/html</a> pour visualiser LIST au format HTML</li>
        <li>L'URL <a href="list/json">list/json</a> pour visualiser LIST au format JSON</li>
        </ul>
        </p>
        </body>
        </html>.toString
    )
  )

  lazy val error404Html = HttpResponse(
    status = 404,
    entity = HttpEntity(`text/html`,
      <html>
        <head>
        <title>Erreur 404</title>
        </head>
        <body>
        <h1>Erreur 404</h1>
        <p>
        La page demandée est inconnue
        </p>
        </body>
        </html>.toString
    )
  )
}
