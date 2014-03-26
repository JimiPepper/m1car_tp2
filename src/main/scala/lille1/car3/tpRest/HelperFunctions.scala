package lille1.car3.tpRest

import MyJsonProtocol._
import org.apache.commons.net.ftp.FTPFile
import spray.http.HttpEntity
import spray.http.HttpResponse
import spray.http.MediaTypes._
import spray.json._

/**
  * Défini l'ensemble des fonctions régulièrements utilisées
  * pour générer une spray.http.HttpResponse de type JSON ou HTML
  *
  * @author Gouzer Willian
  * @author Philippon Romain
  **/
trait HelperFunction extends DefaultJsonProtocol {

  /**
    * Extrait la valeur d'indice idx de l'expression régulière appliquée sur la chaîne de caractères
    * passé en paramètre
    *
    * @param text Contient la chaîne dont on extrait la valeur
    * @param regex Correspond à l'expression régulière à appliquer sur text
    * @param idx Contient le numéro du groupe que l'on souhaite matcher
    * @return La valeur d'indice idx matchée par l'expression régulière regex sur text
    **/
  def extract(text: String, regex: String, idx: Int) : String = {
    val Some(reg) = regex.r.findFirstMatchIn(text)
    reg.group(idx)
  }

   /**
    * Construit une réponse HTTP de type JSON à l'aide de la liste des fichiers contenu et du chemin
    * passés en paramètres
    *
    * @param workingDirectoryPath Contient le chemin du dossier à lister
    * @param files Correspond à la liste des fichiers 
    * @return Produit une spray.http.HttpResponse qui contient la liste des fichiers au format JSON
    **/
  def JSON_ListResponse(workingDirectoryPath : String, files: Array[FTPFile]) : HttpResponse = {
    var responseJSON : List[Array[String]] = List[Array[String]]()
    var array : Array[String] = null

    for(f <- files) {
      if(f.isDirectory) {
        array = new Array[String](3)
        array(0) = "Répertoire"
        array(1) = f.getName()
        array(2) = "http://localhost:8080/list/json"+ workingDirectoryPath +"/"+ f.getName
      }
      else {
        array = new Array[String](4)
        array(0) = "Fichier"
        array(1) = f.getName()
        array(2) = "http://localhost:8080/get"+ workingDirectoryPath +"/"+ f.getName
        array(3) = "http://localhost:8080/delete"+ workingDirectoryPath +"/"+ f.getName
      } 
      
      responseJSON = array :: responseJSON
    }

    HttpResponse(
      status = 200,
      entity = HttpEntity(`application/json`, responseJSON.toJson.toString)
    )
  }

  /**
    * Construit une réponse HTTP de type HTML à l'aide de la liste des fichiers contenu et du chemin
    * passés en paramètres
    *
    * @param workingDirectoryPath Contient le chemin du dossier à lister
    * @param files Correspond à la liste des fichiers 
    * @return Produit une spray.http.HttpResponse qui contient la liste des fichiers au format HTML
    **/
  def HTML_ListResponse(workingDirectoryPath: String, files: Array[FTPFile]) : HttpResponse = {
    var responseHTML = new String("<html><head><meta charset=\"UTF-8\"><title>Commande LIST - HTML</title></head>")
    responseHTML += "<body><h1>Commande LIST FTP - Version HTML</h1><h3>Répertoire courant : "+ workingDirectoryPath +"</h3>"

    responseHTML += "<div><p><em>Légende :</em> <span style=\"color: red;\">Dossier</span></p></div>"
    responseHTML += "<div><ul style=\"list-style: none;\">"

    for(f <- files) {
      f.isDirectory match {
        case true => responseHTML += "<li><a style=\"color: red;\" href=\"http://localhost:8080/list/html"+ workingDirectoryPath +"/"+ f.getName +"\">"+ f.getName +"</a></li>"
        case false => responseHTML += "<li><strong><a href=\"http://localhost:8080/delete"+ workingDirectoryPath +"/"+ f.getName +"\">X</a></strong> <a href=\"http://localhost:8080/get"+ workingDirectoryPath +"/"+ f.getName +"\">"+ f.getName +"</a></li>"
      }
    }
    
    responseHTML += "</ul></div></body></html>"

    HttpResponse(
      status = 200,
      entity = HttpEntity(`text/html`, responseHTML)
    )
  }
}
