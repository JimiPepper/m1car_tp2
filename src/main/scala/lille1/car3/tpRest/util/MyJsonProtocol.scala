package lille1.car3.tpRest.util

import spray.json._
import DefaultJsonProtocol._

/**
* Redéfinit l'objet DefaultJsonProtocol pour ajouter de nouveaux types à la conversion JSON délivré par défaut dans Spray
*
* @author Gouzer Willian
* @author Philippon Romain

**/
object MyJsonProtocol extends DefaultJsonProtocol {
   /**
  * Étend l'objet RootJsonFormat pour permettre la conversion JSON du type List[Array[String]]
  **/
  implicit object ListOfArrayStringJsonFormat extends RootJsonFormat[List[Array[String]]] {
    /**
    * Convertit une liste de tableaux de chaines de caractères en un objet JSON de Spray
    *
    * @param list 
    * @return Retourne un objet JSON prêt à converti en une chaine de caractères respectant le format JSON
    **/
    def write(list: List[Array[String]]) : JsArray = {
      JsArray(list.map(elem => elem.toJson))
    }

    /**
    * Convertit un objet JSON de Spray en une liste de tableaux de chaines de caractères
    *
    * @param value L'objet JSON qui doit être parsé
    * @return Renvoie une liste de tableaux de chaines de caractères
    * @throws deserializationError quand l'objet JSON parsé ne correspond pas au type List[Array[String]]
    **/
    def read(value: JsValue): List[Array[String]] = value match {
      case JsArray(content) => for(JSON_elem <- content) yield JSON_elem.convertTo[Array[String]]
      case _ => deserializationError("List[Array[String] attendu")
    }
  }
}