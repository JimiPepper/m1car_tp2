package lille1.car3.tpRest

import org.apache.commons.net.ftp._

object FtpConnexion {
  lazy val client = new FTPClient
  lazy val connect = (ip:String, port:Int) => client.connect(ip, port)
  lazy val login = (login: String, mdp: String) => client.login(login, mdp)
}
