package com.example

import akka.actor.{ Props, Actor }
import java.io.FileOutputStream
import java.io.FileOutputStream
import spray.http._
import spray.http.MediaTypes._
import spray.routing._
import spray.http.BodyPart
import java.io.{ ByteArrayInputStream, InputStream, OutputStream, FileOutputStream, FileInputStream }


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling

  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    (path("") & get) { // `text/xml` by default, override to be sure
      respondWithMediaType(`text/html`) {
        complete(index)
      }
    } ~
  path("list") {
    (path("html") & get) {
      respondWithMediaType(`text/html`) {
        complete(<html>Test 1</html>)
      }
    } ~
      (path("json") & get) {
      complete("JSON time")
    }
  } ~
  path("getFile") {
    respondWithMediaType(`application/octet-stream`) {
      getFromFile("/tmp/toto.jpg")
    }
  } ~
  path("storeFile") {
    respondWithMediaType(`text/html`) {
      complete(
        <html>
          <h1>File</h1>
          <form name="form1" method="post" enctype="multipart/form-data" action="store">
          <input name="file" type="file"/>
          <input type="submit" value="submit"/>
          </form>
          </html>
      )
    }
  } ~
    (path("store") & post) {
    formField('file.as[Array[Byte]]) { file =>
      val fos : FileOutputStream = new FileOutputStream("test_file")
      try { fos.write(file) }
      finally { fos.close }
      complete { "done" }
    }
  }

  // fin de la route !

  lazy val index = {
    <html>
    <body>
    <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
    <h1>Hey bitch i'm working on Spray :D</h1>
    </body>
    </html>
  }

  private def saveAttachment(fileName: String, content: Array[Byte]): Boolean = {
    saveAttachment[Array[Byte]](fileName, content, {(is, os) => os.write(is)})
    true
  }

  private def saveAttachment(fileName: String, content: InputStream): Boolean = {
    saveAttachment[InputStream](fileName, content,
      { (is, os) =>
        val buffer = new Array[Byte](16384)
        Iterator
          .continually (is.read(buffer))
          .takeWhile (-1 !=)
          .foreach (read => os.write(buffer,0,read))
      }
    )
  }

  private def saveAttachment[T](fileName: String, content: T, writeFile: (T, OutputStream) => Unit): Boolean = {
    try {
      val fos = new FileOutputStream(fileName)
      writeFile(content, fos)
      fos.close()
      true
    } catch {
      case _ : Throwable => false
    }
  }

}
// }
