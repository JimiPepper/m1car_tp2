package lille1.car3.tpRest

import java.io.FileOutputStream
import java.io.InputStream
import org.apache.commons.net.ftp._
import java.lang.Exception

class FtpConnexion(userLogin: String, userMdp: String, serverAddress: String, serverPort: Int) {
  override def toString = s"FtpConnexion($userLogin, $userMdp, $serverAddress, $serverPort)"

  def info : String = {
    userLogin +"_"+ userMdp +"_"+ serverAddress +"_"+ serverPort.toString
  }

  var client = new FTPClient
  
  def connect : Boolean = {
    try {
      client.connect(serverAddress, serverPort)
      true
    }
    catch {
      case e: Exception => false
    }
  }

  def disconnect : Boolean = {
    try {
      client.disconnect
      true
    }
    catch {
      case e: Exception => false
    }
  }

  def login : Boolean = {
    var connected = false
    try {
      if (client.login(userLogin, userMdp)) {
        client.setFileType(FTP.BINARY_FILE_TYPE)
        connected = true
      }

      connected
    }
    catch {
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