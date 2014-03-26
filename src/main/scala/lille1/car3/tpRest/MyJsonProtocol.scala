package lille1.car3.tpRest

import spray.json._
import DefaultJsonProtocol._

/* OVERRIDE OBJECTS */
object MyJsonProtocol extends DefaultJsonProtocol {
  // on ajoute le type List[Map, Map] au types de bases supporté par Spray pouvant être sérialisé au format JSON
  implicit object MyJsonFormat extends RootJsonFormat[List[Map[String, String]]] {
    def returnJSONElement(array : Array[String]) : JsObject = {

      array(0) match {
        case "Répertoire" => JsObject("filename" -> JsString(array(1)), "type" -> JsString(array(0)), "LIST_url" -> JsString(array(2)))
        case "Fichier" => JsObject("filename" -> JsString(array(1)), "type" -> JsString(array(0)), "GET_url" -> JsString(array(2)), "DEL_url" -> JsString(array(3)))  
        case _ => JsObject() 
      }
       
    } 

    def write(list: List[Array[String]]) = {
      JsArray(list.map(elem => returnJSONElement(elem)))
    }

    def read(value: JsValue): List[Array[String]] = value match {
      case JsArray(content) => {
        var response : List[Array[String]] = List[Array[String]]()
        var array : Array[String] = null

        for(JSON_elem <- content) {
          array = JSON_elem.asJsObject.getFields("type") match {
            case JsValue(type_fichier) => type_fichier match {
              case "Répertoire" => JSON_elem.asJsObject.getFields("filename", "type", "LIST_url") match {
                case Seq(JsString(filename), JsString(type_data), JsString(LIST_url)) => Array(type_data, filename, LIST_url)
                case _ => deserializationError("Erreur de formatage ~ le JSON attendu ne correspnd au format List[Map[String, String]]")
              }
              case "Fichier" => JSON_elem.asJsObject.getFields("filename", "type", "GET_url", "DEL_url") match {
                case Seq(JsString(filename), JsString(type_data), JsString(GET_url), JsString(DEL_url)) => Array(type_data, filename, GET_url, DEL_url)
                case _ => deserializationError("Erreur de formatage ~ le JSON attendu ne correspnd au format List[Map[String, String]]")
              } 
            case _ => deserializationError("Erreur de formatage ~ le JSON attendu ne correspnd au format List[Map[String, String]]")
            }
          }

          response = array :: responseJSON
        }

        response
      }
      case _ => deserializationError("List[Map[String, String]] attendu")
    }
  }
}
