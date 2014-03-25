package lille1.car3.tpRest

import java.io.FileOutputStream
import java.io.InputStream
import org.apache.commons.net.ftp._
import scala.throws

class FtpConnexion(userLogin: String, userMdp: String, serverAddress: String, serverPort: Int) {
  override def toString = s"FtpConnexion($userLogin, $userMdp, $serverAddress, $serverPort)"

  def info : String = userLogin + "_" + userMdp + "_" + serverAddress + "_" + serverPort.toString

  var client = new FTPClient
  var connected: Boolean = false

  @throws[java.io.IOException]("The ip/port doesn't exist")
    def connect : Unit = {
    client.connect(serverAddress, serverPort)
  }

  @throws[java.io.IOException]("Already disconnected")
  def disconnect : Unit = {
    client.disconnect
  }

  @throws[java.io.IOException]("The login/mdp doesn't exist")
  def login : Boolean = {
    if (client.login(userLogin, userMdp)) {
      client.setFileType(FTP.BINARY_FILE_TYPE)
      connected = true
    }
    connected // force le type de retour en booleen
  }

  @throws[java.io.IOException]("Already logout")
  def logout : Boolean = {
    client.logout
  }

  @throws[java.io.IOException]("The path doesn't exist")
  def list(path: String) : Array[FTPFile] = {
    client.listFiles(path)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def upload(file: String, is: InputStream) : Boolean = {
    client.storeFile(file, is)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def download(file: String, fos: FileOutputStream) : Boolean = {
    client.retrieveFile(file, fos)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def delete(file: String) : Boolean = {
    client.deleteFile(file)
  }

}
