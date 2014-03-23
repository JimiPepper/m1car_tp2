package lille1.car3.tpRest

import java.io.FileOutputStream
import java.io.InputStream
import org.apache.commons.net.ftp._
import scala.throws

class FtpConnexion {
  var client = new FTPClient
  var connected: Boolean = false

  def connect (ip: String, port: Int) {
    client.connect(ip, port)
  }

  def login (login: String, mdp: String) {
    if (client.login(login, mdp)) {
      client.pasv
      client.setFileType(FTP.BINARY_FILE_TYPE)
      client.enterLocalPassiveMode
      connected = true
    }
  }

  @throws[java.io.IOException]("The path doesn't exist")
  def list(path: String) {
    // TODO, list
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def upload(file: String, is: InputStream) {
    client.storeFile(file, is)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def retrieve(file: String, fos: FileOutputStream) {
    client.retrieveFile(file, fos)
  }

  @throws[java.io.IOException]("The file doesn't exist")
  def delete(file: String) {
    client.deleteFile(file)
  }


}
