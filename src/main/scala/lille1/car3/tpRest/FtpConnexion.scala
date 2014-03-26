package lille1.car3.tpRest

import java.io.FileOutputStream
import java.io.InputStream
import org.apache.commons.net.ftp._
import java.io.Exception

class FtpConnexion(login: String, mdp: String, serverAddress: String, serverPort: Int) {
  override def toString = s"FtpConnexion($login, $mdp, $serverAddress, $serverPort)"

  def info : String = {
    login +"_"+ mdp +"_"+ serverAddress +"_"+ serverPort.toString
  }

  var client = new FTPClient
  
  def connect : Boolean = {
    try {
      client.connect(serverAddress, serverPort)
      true
    }
    catch(Exception e) {
      case e: Exception => false
    }
  }

  def disconnect : Boolean = {
    try {
      client.disconnect
      true
    }
    catch(Exception e) {
      case e: Exception => false
    }
  }

  def login : Boolean = {
    try {
      if (client.login(login, mdp)) {
        client.setFileType(FTP.BINARY_FILE_TYPE)
        true
      }

      false
    }
    catch(Exception e) {
      case e: Exception => false
    }
  }

  def logout : Boolean = {
    try { 
      client.logout
    } 
    catch {
      case e: Exception => false
    }
  }

  def list(path: String) : Array[FTPFile] = {
    try { 
      client.listFiles(path)
    } 
    catch {
      case e: Exception => Array[FTPFile]()
    }
  }

  def upload(file: String, is: InputStream) : Boolean = {
    try { 
      client.storeFile(file, is)
    } 
    catch {
      case e: Exception => false
    }
  }

  def download(file: String, fos: FileOutputStream) : Boolean = {
    try { 
      client.retrieveFile(file, fos)
    } 
    catch {
      case e: Exception => false
    }
  }

  def delete(file: String) : Boolean = {
    try { 
      client.deleteFile(file)
    } 
    catch {
      case e: Exception => false
    }
  }
}