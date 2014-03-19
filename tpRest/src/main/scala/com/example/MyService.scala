package com.example

import akka.actor.{ Props, Actor }
import java.io.FileOutputStream
import java.io.FileOutputStream
import spray.http._
import spray.http.MediaTypes._
import spray.routing._
import spray.http.BodyPart
import java.io.{ ByteArrayInputStream, InputStream, OutputStream, FileOutputStream, FileInputStream }


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling

  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

	val html: Boolean = true; 

  val myRoute =
    (path("") & get) { // `text/xml` by default, override to be sure
      respondWithMediaType(`text/html`) {
        complete(index)
      }
    } ~
  path("list") {
    (path("html") & get) {
      respondWithMediaType(`text/html`) {
        complete(<html>Test 1</html>)
      }
    } ~
      (path("json") & get) {
      complete("JSON time")
    }
  } ~
  path("getFile") {
    respondWithMediaType(`application/octet-stream`) {
      getFromFile("/tmp/toto.jpg")
    }
  } ~
  path("storeFile") {
    respondWithMediaType(`text/html`) {
      complete(
        <html>
          <h1>File</h1>
          <form name="form1" method="post" enctype="multipart/form-data" action="store">
          <input name="file" type="file"/>
          <input type="submit" value="submit"/>
          </form>
          </html>
      )
    }
  } ~
    (path("store") & post) {
    formField('file.as[Array[Byte]]) { file =>
      val fos : FileOutputStream = new FileOutputStream("test_file")
      try { fos.write(file) }
      finally { fos.close }
      complete { "done" }
    } ~
	path("loginAction")
	{
		complete("Ok !");
		//redirect("list/html");
	} ~
	path("changeFormatting")
	{
		if(html) html = false else html = true ; // redirect 
		// html -> true : on envoie en html
		// html -> false : on envoie du JSON
	}
  }

  // fin de la route !

  lazy val logWebPage_html = {
    <html>
    <body>
    <h1>Connexion - FTP</h1>

	<form type="post" action="loginAction">
    <div>
	<h3>Serveur FTP</h3>
	<input type="text" name="ip_server" value="Entrez une adresse IP..." />	
	<input type="text" name="port_server" value="Entrez un port..." />
</div>

<div>
<h3>Utilisateur</h3>
	<label for="login">Identifiant : </label><input type="text" name="login" id="login" value="Votre identifiant" /><br />
	<label for="mdp">Mot de passe : </label><input type="password" name="mdp" id="mdp" /><br />
	<input type="submit" value="Se connecter" />
</div>
</form>
    </body>
    </html>
  }

  private def saveAttachment(fileName: String, content: Array[Byte]): Boolean = {
    saveAttachment[Array[Byte]](fileName, content, {(is, os) => os.write(is)})
    true
  }

  private def saveAttachment(fileName: String, content: InputStream): Boolean = {
    saveAttachment[InputStream](fileName, content,
      { (is, os) =>
        val buffer = new Array[Byte](16384)
        Iterator
          .continually (is.read(buffer))
          .takeWhile (-1 !=)
          .foreach (read => os.write(buffer,0,read))
      }
    )
  }

  private def saveAttachment[T](fileName: String, content: T, writeFile: (T, OutputStream) => Unit): Boolean = {
    try {
      val fos = new FileOutputStream(fileName)
      writeFile(content, fos)
      fos.close()
      true
    } catch {
      case _ : Throwable => false
    }
  }
  
  private def getJSONStringList(s: String): String = 
  {
   // val list: List[String] = s.
null;
  }

}
// }

/*
* Liste des tests à implémenter :
*	-> Vérifier qu'un utilisateur non-loggué ne puisse pas accéder à l'application (renvoi d'une exception)
*	-> Si un utilisateur ne fait rien pendant x secondes/minutes, le déconnecter
*	-> Si un utilisateur non-loggué/connecté tente d'accéder à une page qui n'existe pas (william_est_un_as_d_emacs) renvoyer une exception
*	-> Si un utilisateur tente de RETR un fichier qui n'existe pas, renvoyer une exception
*	-> Si l'utilisateur désire afficher une réponse en HTML il ne peut pas avoir une réponse en JSON 
*	-> Si un utilisateur veut se déplacer dans un enfant du répertoire courant, il le peut
*	-> Si l'utilisateur veut PUT un fichier sur le serveur sans passer par storeFile, renvoyer une exception
*	-> Si l'utilisateur souhaite qu'on lui renvoie du html, on lui envoie du html
*	-> Si l'utilisateur renvoie du JSON, on lui envoie du JSON
*	-> Quand l'utilisateur se déconnecte, il est bien déconnecté du serveur FTP
*	-> De manière générale, si le serveur FTP renvoie une code d'erreur, renvoyer une exception (CODE >= 300)
*	-> 
*/
