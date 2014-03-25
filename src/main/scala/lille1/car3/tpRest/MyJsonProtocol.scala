package lille1.car3.tpRest

import spray.json._
import DefaultJsonProtocol._

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
