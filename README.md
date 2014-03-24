* w.gouzer
* r.philippon

PASSERELLE REST
===============

PRÉSENTATION
------------
Ce logiciel est une passerelle REST pour serveur FTP développé en
**`SCALA`** à l'aide de la librairie [Spray](http://spray.io/).
Il utilise la librairie **`JAVA`**
[`org.apache.commons.net.ftp`](http://commons.apache.org/proper/commons-net/)
pour effectuer les connexions aux serveur ainsi que pour exécuter les
commandes FTP tels que *LIST*, *RETR* ou *STOR* par exemple.


ARCHITECTURE
------------


Paquetages
==========

Le projet est découpé en un unique package `lille1.car3.tpRest` qui
contient l'ensemble des classes/objets et traits pour lancer la passerelle.


Contenu
=======

Nous retrouvons à l'intérieur 3 éléments essentiels :

* l'Objet `Boot` qui étend la classe `App` de **`SCALA`** qui lance la
passerelle en donnant la main à l'acteur `RoutingActor`.
* La classe `RoutingActor` qui extend la classe Actor de la librairie
`**Akka**` en implément le trait `RootingService`. Cet acteur s'occupe
de récupérer les messages HTTP et en fonction de ces messages, de
rediriger
le client vers le path associé afin de renvoyer des réponses HTTP au client.
* Le trait `RoutingService` contient la définition du routing de notre
application. Il permet de dissocier la définition du routing de sa gestion (prise
en charge nous le rappelons par `RoutingActor`. Il contient égalements des
Helpers HTML pour renvoyer des réponses HTML pré-défini au client. Il surcharge
également l'objet `DefaultJsonProtocol` (contenu dans le package `spray.json`)
qui permet la conversion au format `JSON` le type
`List[Map[String, String]]` que nous utilisons pour récupérer les informations
nécessaires du type `FTPFile[]` fourni la librairie `org.apache.commons.net.ftp`.


Exceptions
==========

TESTS UNITAIRES
---------------

SAMPLES
-------

Conversion JSON
===============

La conversion JSON est très simple avec `Spray` et nécessite très peu de code pour être fonctionnel :

def returnJSONElement() : JsObject = JsObject("filename" -> JsString("toto"),
                                               "chiffre" -> JsNumber(10))

def write(list: List[Map[String, String]]) = JsArray(list.map(elem => returnJSONElement))

def JSON_ListResponse(files: Array[FTPFile]) : String = {
  var responseJSON : List[Map[String, String]] = List[Map[String, String]]()
  var map : Map[String, String] = Map[String, String]()

  for(f <- files) {
    map + ("filename" -> f.getName())
    responseJSON = map :: responseJSON
    /* ... */
    map = Map[String, String]()
  }

  responseJSON.toJson.toString
}
`
