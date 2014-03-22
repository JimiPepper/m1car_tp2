package lille1.car3.tpRest

import org.apache.commons.net.ftp._

class FtpConnexion {
  val client = new FTPClient
  val connect = client.connect("ftp.mozilla.org", 21)
  val login = client.login("anonymous","")
}
