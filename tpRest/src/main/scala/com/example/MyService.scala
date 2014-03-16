package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

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
trait MyService extends HttpService 
{

  val myRoute =
    path("") 
    {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
        	  	<h1>Hey bitch i'm working on Spray :D</h1>
              </body>
            </html>
          }
        }
      }
    } ~
    pathPrefix("list")
    {
      path("html")
      {
	      get
	      {
	        respondWithMediaType(`text/html`)
	        {
	          val list = (0 to 10).toList
	          complete
	          {
	        	  <html>
        	  	<body>
        	  		<ul>
        	  			<li>Test 1</li>
        	  			<li>Test 2</li>
        	  			<li>Test 3</li>
        	  			<li>Test 4</li>
        	  			<li>Test 5</li>
        	  		</ul>
        	  	</body>
        	  </html>
	          }
	        }
	      }
      } ~
      path("json")
      {
        complete("JSON time")
      }
    } ~
    path("complete")
    {
      get
      {
        println("test")
        complete("Misa Campo had been transfered by the sray function complete()")
      }
    } ~
    path("nombre" / LongNumber) 
    {
      nombre =>
      get
      {
        complete(nombre+"")
      }
    }
}