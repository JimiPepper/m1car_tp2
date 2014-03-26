package lille1.car3.tpRest

import MyJsonProtocol._
import org.apache.commons.net.ftp.FTPFile
import spray.http.HttpEntity
import spray.http.HttpResponse
import spray.http.MediaTypes._
import spray.json._


trait HelperFunction extends DefaultJsonProtocol {
  def extract(text: String, regex: String, idx: Int) : String = {
    val Some(reg) = regex.r.findFirstMatchIn(text)
    reg.group(idx)
  }

  /* HELPERS FUNCTIONS */
  def JSON_ListResponse(files: Array[FTPFile]) : HttpResponse = {
    var responseJSON : List[Map[String, String]] = List[Map[String, String]]()
    var map : Map[String, String] = Map[String, String]()

    for(f <- files) {
      map + ("filename" -> f.getName())
      /* ... */
      responseJSON = map :: responseJSON
      map = Map[String, String]()
    }

    HttpResponse(
      status = 200,
      entity = HttpEntity(`text/html`, responseJSON.toJson.toString)
    )
  }

  def HTML_ListResponse(pathname: String, files: Array[FTPFile]) : HttpResponse = {
    var responseHTML = new String("<html><head><meta charset=\"UTF-8\"><title>Commande LIST - HTML</title></head>")
    responseHTML += "<body><h1>Commande LIST FTP - Version HTML</h1><h3>Répertoire courant : "+ pathname +"</h3>"

    responseHTML += "<div><p><em>Légende :</em> <span style=\"color: red;\">Dossier</span></p></div>"
    responseHTML += "<div><ul style=\"list-style: none;\">"

    for(f <- files) {
      f.isDirectory match {
        case true => responseHTML += "<li><a style=\"color: red;\" href=\"http://localhost:8080/list/html"+ pathname +"/"+ f.getName +"\">"+ f.getName +"</a></li>"
        case false => responseHTML += "<li><strong><a href=\"http://localhost:8080/delete"+ pathname +"/"+ f.getName +"\">X</a></strong> <a href=\"http://localhost:8080/get"+ pathname +"/"+ f.getName +"\">"+ f.getName +"</a></li>"
      }
    }

    
    responseHTML += "</ul></div></body></html>"
    /*
    if (pathname != "" ) {
      if (pathname.lastIndexOf("/") != -1) {
        responseHTML += "<a href='/list/html/" + pathname.substring(0, pathname.lastIndexOf("/")) +
        "'onclick=\"list(\'/list/html/"+pathname.substring(0,pathname.lastIndexOf("/"))+
"\');return false;\" ><b>..</b></a<br/>"
      } else {
        responseHTML += "<a href='/list/html/default' onclick=\'list(\"/list/html/default\");return false;\' ><b>..</b></a><br/>"
      }
    }

    // met en forme la chaine représentant le path (i.e. supprime les '/' inutiles)
    var path = {
      if (pathname.length >= 1) {
        pathname(0) match { // retire le premier / du chemin
          case '/' => pathname.slice(1, pathname.length) + "/"
          case _ => pathname + "/"
        }
      } else ""
    }

    responseHTML += """<ul style="list-style: none;">"""
    for(f <- files) {
      f.isDirectory match {
        case true =>
          responseHTML += "<li><b><a href=\"/list/html/" + path + f.getName +
          "\" onclick=\"list(\'"+"/list/html/"+ path + f.getName + "\'); return false;\">" +
          f.getName() + "/</a></b></li>"
        case false =>
          responseHTML += "<li><a href='/delete/"+f.getName+
          "' onclick=\'deleteFile(\"/delete/"+f.getName + "\");return false;\' ><b>[</b>X<b>]</b></a>"
          responseHTML += "  <a href=\"/get/"+path+f.getName+"\">"+f.getName()+ "</a></li>"

      }

    }
    responseHTML += "</ul>"

    responseHTML += """<h1>Commande STORE - Déposer votre fichier sur le serveur FTP</h1>"""
    responseHTML += """<form name="storeForm" method="post" enctype="multipart/form-data" action="/store/file">"""
    responseHTML += """<input name="file" type="file" /><input type="submit" value="Déposer" /></form>"""


    responseHTML += "</body></html>"
    */

    HttpResponse(
      status = 200,
      entity = HttpEntity(`text/html`, responseHTML)
    )
  }
}
