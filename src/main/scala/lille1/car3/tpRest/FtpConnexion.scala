package lille1.car3.tpRest

import java.io.FileOutputStream
import java.io.InputStream
import org.apache.commons.net.ftp._
import scala.throws

class FtpConnexion {
  var client = new FTPClient
  var connected: Boolean = false

  @throws[java.io.IOException]("The ip/port doesn't exist")
  def connect(ip: String, port: Int) : Unit = {
    client.connect(ip, port)
  }

  @throws[java.io.IOException]("Already disconnected")
  def disconnect : Unit = {
    client.disconnect
  }

  @throws[java.io.IOException]("The login/mdp doesn't exist")
  def login(login: String, mdp: String) : Boolean = {
    if (client.login(login, mdp)) {
      client.pasv
      client.setFileType(FTP.BINARY_FILE_TYPE)
      client.enterLocalPassiveMode
      connected = true
    }
    connected
  }

  @throws[java.io.IOException]("Already logout")
  def logout : Boolean = {
    client.logout
  }

  @throws[java.io.IOException]("The path doesn't exist")
  def list(path: String) : Array[FTPFile] = {
    // TODO list & change return type if needed
    // val files = client.listFiles(path)
    client.listFiles(path)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def upload(file: String, is: InputStream) : Boolean = {
    client.storeFile(file, is)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def retrieve(file: String, fos: FileOutputStream) : Boolean = {
    client.retrieveFile(file, fos)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def delete(file: String) : Boolean = {
    client.deleteFile(file)
  }
}
