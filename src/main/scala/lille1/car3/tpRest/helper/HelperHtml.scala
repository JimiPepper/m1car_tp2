package lille1.car3.tpRest.helper

import spray.http._
import spray.http.MediaTypes._

/**
  * Trait permettant de séparer, simplifier et factoriser la gestion des codes HTML renvoyés
  * par notre application
  *
  * @author Gouzer Willian
  * @author Philippon Romain
  **/
trait HelperHtml {
  /**
  * Contient le formulaire de connexion à la passerelle REST
  **/
  lazy val loginForm = HttpResponse(
    entity = HttpEntity(`text/html`,
        <html>
        <head>
            <title>Passerelle REST</title>
        </head>
        <body>
            <h1>Connexion - FTP</h1>

            <form name="loginForm" method="post" action="login-action">
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

  /**
  * Contient le formulaire pour héberger un fichier sur un serveur FTP
  **/
  lazy val storeForm = HttpResponse(
    entity = HttpEntity(`text/html`,
        <html>
        <head>
            <title>Déposer un fichier</title>
        </head>
        <body>
            <h1>Commande STORE - Déposer votre fichier sur le serveur FTP</h1>

            <form name="storeForm" method="post" enctype="multipart/form-data" action="send">
              <label for="path">Chemin absolu où stocker le fichier : </label> <input type="text" name="path" id="path" /><br />
              <input name="file" type="file" />
            <input type="submit" value="Déposer" />
            </form>
        </body>
        </html>.toString
    )
  )

  /**
  * Contient les indications d'utilisations pour la passerelle FTP
  **/
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

            <p>
              Par ailleurs, pour :
              <ul>
                <li><strong>Telecharger un fichier : </strong>Utiliser le mot get suivi du lien sur le serveur FTP pour telecharger ce fichier, exemple : get/toto.txt</li>
                <li><strong>Déposer un fichier : </strong>Rendez-vous <a href="store">sur ce lien</a></li>
                <li><strong>Supprimer un fichier : </strong> Utiliser le mot delete suivi du lien sur le serveur FTP pour telecharger ce fichier, exemple : delete/toto.txt</li>
                <li><strong>Vous deconnecter : </strong>Rendez-vous <a href="logout">ici</a></li>
              </ul>
            </p>
        </body>
        </html>.toString
    )
  )

  /**
  * Contient la page d'erreur HTTP 404
  **/
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

  /**
  * Contient le message de succès de suppression d'un fichier distant sur serveur FTP
  **/
  lazy val deleteDoneMessage = HttpResponse(
    status = 200,
    entity = HttpEntity(`text/html`,
      <html>
      <head>
        <title>Suppression réussie</title>
      </head>
      <body>
        <h1>Le fichier est bien supprimé</h1>
      </body>
      </html>.toString
    )
  )

  /**
  * Contient le message de succès d'upload d'un fichier sur un serveur FTP
  **/
  lazy val storeDoneMessage = HttpResponse(
    status = 200,
    entity = HttpEntity(`text/html`,
      <html>
      <head>
        <title>Upload réussie</title>
      </head>
      <body>
        <h1>Le fichier est bien uploadé sur le serveur FTP</h1>
      </body>
      </html>.toString
    )
  )

  /**
  * Contient le message de succès d'authentification utilisateur
  **/
  lazy val loggedInDoneMessage = HttpResponse(
    status = 200,
    entity = HttpEntity(`text/html`,
      <html>
      <head>
        <title>Authentification réussie</title>
      </head>
      <body>
        <h1>Vous êtes bien authentifié sur le serveur FTP</h1>

        <p>
          Pour poursuivre votre navigation, veuillez vous <a href="http://localhost:8080/list">rendre ici</a>
        </p>
      </body>
      </html>.toString
    )
  )
}
