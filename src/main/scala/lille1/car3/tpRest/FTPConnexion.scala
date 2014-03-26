package lille1.car3.tpRest

import java.io.FileOutputStream
import java.io.InputStream
import org.apache.commons.net.ftp._
import java.lang.Exception
import java.io.FileNotFoundException

/**
  * Abstrait et enveloppe la classe org.apache.commons.net.ftp.FTPClient autour de la classe permettant
  * d'intéragir avec le serveur FTP tout en gérant les exceptions renvoyé par FTPClient
  *
  * @constructor crée une connexion FTP avec un login, un mot de passe client ainsi qu'une adresse du 
  * serveur FTP et le port utilisé pour la connexion
  * @param userLogin correspond au login client
  * @param userMdp correspond au mot de passe client
  * @param serverAddress correspond à l'adresse du serveur FTP
  * @param serverPort correspond au numéro du port utilisé pour se connecter au serveur FTP
  * @author Gouzer Willian
  * @author Philippon Romain
  **/
class FTPConnexion(userLogin: String, userMdp: String, serverAddress: String, serverPort: Int) {
  override def toString = s"FtpConnexion($userLogin, $userMdp, $serverAddress, $serverPort)"

  /**
    * Affiche le login, le mot de passe,l'adresse du serveur FTP et le port utilisé pour s'y connecter
    *
    * @return Les informations utilisées pour créer une instance de FTPConnexion sous la forme la forme login_mot de passe_serveur ip_serveur port
    **/
  def info : String = {
    userLogin +"_"+ userMdp +"_"+ serverAddress +"_"+ serverPort.toString
  }

  /**
    * Représente la connexion serveur FTP
    **/ 
  var client = new FTPClient
  
  /**
    * Crée une connexion entre le serveur et l'objet FTPCLient
    *
    * @return Renvoie true si la connexion réussie, false si elle échoue
    **/  
  def connect : Boolean = {
    try {
      client.connect(serverAddress, serverPort)
      true
    }
    catch {
      case e: Exception => false
    }
  }

  /**
    * Lance une connexion cliente entre le serveur FTP et l'objet FTP en utilisant le login et
    * le mot passe passé à l'objet FTPConnexion lors de son instanciation en mode binaire
    *
    * @return Renvoi true si l'authentification réussie, false sinon
    **/  
  def login : Boolean = {
    var connected = false
    try {
      if(client.login(userLogin, userMdp)) {
        client.setFileType(FTP.BINARY_FILE_TYPE)
        connected = true
      }

      connected
    }
    catch {
      case e: Exception => false
    }
  }

  /**
    * Liste les fichiers & dossiers contenu à l'adresse path passée en paramètre
    *
    * @param path Le chemin du dossier à lister
    * @return Retourne la liste des fichiers et dossiers contenu dans path
    * @throw FileNotFoundException quand le dossier passé en paramètre n'existe pas dans
    * l'arborescence du serveur FTP
    **/  
  def list(path: String) : Array[FTPFile] = {
    try { 
      if(path.equals("") || client.changeWorkingDirectory(path))
        client.listFiles("")
      else
        throw new FileNotFoundException("Aucun dossier n'a été trouvé à "+ path) 
    } 
    catch {
      case e: Exception => throw new FileNotFoundException("Aucun dossier n'a été trouvé à "+ path)
    }
  }

  /**
    * Envoi une commande d'upload (STOR) au serveur FTP pour envoyer le fichier passé en paramètre
    *
    * @param file Contient le nom du du fichier à déposer sur le serveur FTP
    * @param is Reçoit le stream des données du fichier que l'on souhaite envoyer
    * au serveur FTP
    * @return Renvoie true si l'upload a fonctionné, false sinon
    **/
  def upload(file: String, is: InputStream) : Boolean = {
    try {
      client.storeFile(file, is)
    } 
    catch {
      case e: Exception => false
    }
  }

  /**
    * Lance une commande de téléchargement sur le fichier passé en paramètre
    *
    * @param file Contient le nom du fichier qui sera téléchargé
    * @param fos Reçoit le stream des données envoyées par le serveur FTP
    * @return Renvoie true si le téléchargement réussi, false sinon
    **/
  def download(file: String, fos: FileOutputStream) : Boolean = {
    try { 
      client.retrieveFile(file, fos)
    } 
    catch {
      case e: Exception => false
    }
  }

  /**
    * Lance une commande de supression au serveur FTP pour le fichier passé en paramètre
    *
    * @param file Correspond au nom du fichier qui sera supprimé par le serveur FTP
    * @return Renvoie true si la supression est effectuée, false sinon
    **/
  def delete(file: String) : Boolean = {
    try { 
      client.deleteFile(file)
    } 
    catch {
      case e: Exception => false
    }
  }
}