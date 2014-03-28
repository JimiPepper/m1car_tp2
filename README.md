<!-- Cheatsheet, everything you need to know about markdown format : -->
<!-- https://github.com/adam-p/markdown-here/wiki/Markdown-Here-Cheatsheet#code -->

PASSERELLE REST POUR SERVEUR FTP
================================

* W1gz
* JimiPepper

27/03/2014

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

###Paquetages####

Le projet est composé d'un package gloal `lille1.car3.tpRest` qui
contient l'ensemble des classes/objets et traits pour lancer la passerelle.

Chaque unité fonctionnelle du projet est contenue dans un package spécifique :
  1. `lille1.car3.tpRest.main` qui sert de point d'entrée pour l'exécution de notre programme avec 
  l'objet `Boot`
  2. `lille1.car3.tpRest.actor` contient les acteurs Akka (en l'occurence `RoutingActor`)
  utilisé par le programme.
  3. `lille1.car3.tpRest.helper` englobe l'ensemble des traits contenant des méthodes ou valeurs
  utilisé régulièrement par le programme.
  4. `lille1.car3.tp3.tpRest.util` comprend tous les objets qui paramètre notre application comme
  la déclaration de notre routing dans `RouteService` 
  5. `lille1.car3.tpRest.rejection` contient RejectionHandlerRouting qui redéfini la gestion des erreurs de routing
  implémentée par défaut avec [Spray](http://spray.io/).
  
###Interface###

*Note : L'utilisation a proprement parlé des interfaces en **`SCALA`** n'existe, à la place il existe
des structures `Trait` semblables à des classes que l'on peut considérer comme semi-abstraites.*

* `RoutingService` implémenté dans `RoutingActor` contenant l'ensemble du rooting de l'application et des opérations
que doit géré `RoutingActor` pour faire tourner correctement l'application.
* `HelperHtml` implémenté dans `RootingService` pour récupérer et réutiliser tous les helpers déclarés dans HelperHtml.
* `HelperFunction` implementé dans `RoutingService` pour les mêmes raisons citées ci-dessus.
* `RejectionHandlerRouting` implémenté dans `RoutingService` afin de récupérer la gestion des rejections de [Spray](http://spray.io/).

###Relations d'héritages###

* `Boot` hérite de `scala.App` afin de surcharger la méthode `main(args: Array[String]): Unit` servant de point d'entrée à l'application.
* `RoutingActor` hérite `akka.actor.Actor` afin de bénéficier des opérateurs propres à `akka.actor.Actor` permettant
le parallélisme de notre application sans passer par des threads.
* `MyJsonProtocol` hérite de `spray.json.DefaultJsonProtocol` qui surcharge son objet `RootJsonFormat` afin de définir
une méthode `write(list : List[Array[String]]) : JsArray` et `read(value : JsValue) : List[Array[String]]` permettant pour la première 
la conversion d'une liste de type `List[Array[String]]` en un objet JSON (`spray.json.JsArray`) non prise en charge par défaut par 
[Spray](http://spray.io/). La seconde effectue la conversion inverse.
* Une redéfinition de la classe `RejectionHandler` contenu dans la valeur `myRejectionHandler` dans `RejectionHandlerRouting` permet 
d'ajouter/remplacer la gestion des Rejections défini par défaut dans [Spray](http://spray.io/), elle permet ici de redéfinir le code d'erreur et le
contenu des réponses HTTP renvoyé en cas d'erreurs.
* `HttpService` est étendu par `RootingService`. Cela permet à ce dernier d'être considéré comme un service HTTP et donc
de pouvoir y déclarer à l'intérieur un routing via la valeur `rooting` qui peut être géré par `RoutingActor`.


###Exceptions###

Nous renvoyons des exceptions dans :

* `FTPConnection` : `FileNotFoundException` dans `list(path: String) : Array[FTPFile]` à la ligne 91 pour signifier
que le répertoire que l'on souhaite lister n'existe pas dans l'arborescence du serveur FTP.
* `MyJsonProtocol` : `deserializationError` à l'intérieur de `MyJsonFormat` pour la méthode `read(value: JsValue): List[Array[String]]`
pour signaler que le parsage JSON vers `List[Array[String]]` est impossible pour `value` (en interne cela permet de propager l'erreur
et de tester le parsage pour d'autres types définis soit par la librairie [Spray](http://spray.io/), soit manuellement pour le développeur).

L'utilisation de la structure `try ... catch` dans notre projet se trouve :

* `RejectionHandlerRouting` dans `myRejectionHandler` via le pattern matching pour renvoyer une réponse HTTP personnalisée
en fonction de l'erreur renvoyé par `RoutingActor`.
* `FTPConnexion` dans `connect : Boolean`, `login : Boolean`, `list(path: String) : Array[FTPFile]`, `download(file: String, fos: FileOutputStream) : Boolean`,
`upload(file: String, is: InputStream) : Boolean`, `delete(file: String) : Boolean` pour récupérer les exceptions soulevés par les méthodes de `FTPClient` 
afin de renvoyer un booléen qui sera traité par notre `RoutingActor` afin de renvoyer les réponses HTTP correspondantes.
* `RoutingService` pour les routes utilisant la méthode `list(path: String) : Array[FTPFile]` de `FTPConnexion` aux lignes 65, 85, 106 et 126 pour vérifier
que le dossier listé existe. Également par pattern matching aux lignes 84, 125, 148, 168 afin de valider le chemin pour lister, supprimer, obtenir un
fichier dans l'arborescence du serveur FTP n'est pas vide qui récupéré par le mot clé `Segments`.

SAMPLES
-------

###Conversion JSON###

La conversion JSON est très simple avec [Spray](http://spray.io/) et nécessite très peu de code pour être fonctionnel (`MyJsonProtocol`) :
<!-- Cheatsheet pour des infos sur le formatage du code sur -->
<!-- markdown/github -->

```scala
    def write(list: List[Array[String]]) : JsArray = {
      JsArray(list.map(elem => elem.toJson))
    }
```

```scala
    def read(value: JsValue): List[Array[String]] = value match {
      case JsArray(content) => for(JSON_elem <- content) yield JSON_elem.convertTo[Array[String]]
      case _ => deserializationError("List[Array[String] attendu")
    }
```
##Gestion des erreurs de routing##

L'utilisation du pattern matching permet de gérer de manière élégante les erreurs de rooting (`RejectionHandlerRouting`) : 
```scala
      implicit val myRejectionHandler = RejectionHandler {
        case MissingCookieRejection(cookieName) :: _ =>
          complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Vous devez d'abord <a href=\"http://localhost:8080\">vous authentifiez</a> pour utiliser ce service</p>")))
        case ValidationRejection(message, cause) :: _ =>
          complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Votre session a expiré, veuillez <a href=\"http://localhost:8080\">vous reconnectez</a>")))
        case UnacceptedResponseContentTypeRejection(param) :: _ =>
          complete(HttpResponse(status = 403, entity = HttpEntity(`text/html`, "<p>Erreur 404, la ressource recherchée n'existe pas")))
      }
```
##Example de définition d'une route de la passerelle##

La définition d'une route avec des gardes permet de contrôler le comportement de notre application (`RootingService`)
```scala
      pathPrefix("list" / "html") {
        (pathEnd & get) {
          cookie("ftp_connexion") { cookie_ftp =>
            var tab : Array[String] = cookie_ftp.content.split('_')
            var connexion = new FTPConnexion(tab(0), tab(1), tab(2), tab(3).toInt)

            connexion.connect
            validate(connexion.login, "Vous devez être authentifié pour accéder à ces fonctionnalités") {
              connexion.login
              try { 
                var liste_files : Array[FTPFile] = connexion.list("")
                complete(HTML_ListResponse("", liste_files))
              } catch {
                case fnfe: FileNotFoundException => complete(HttpResponse(status = StatusCodes.Forbidden, entity = HttpEntity(`text/plain`, "Le dossier que vous voulez lister n'existe pas sur le serveur FTP")))
              }
            }
          }
        }
```

REMARQUES
---------