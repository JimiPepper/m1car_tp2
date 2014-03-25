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
}
