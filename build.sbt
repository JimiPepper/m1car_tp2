import AssemblyKeys._

organization  := "lille1.car3.tpRest"

version       := "0.1"

scalaVersion  := "2.10.3"

assemblySettings

jarName in assembly := "tp2-gouzer-william-philippon-romain.jar"

mainClass in assembly := Some("lille1.car3.tpRest.main.Boot")

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "Spray Repo" at "http://repo.spray.io/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "spray" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val sprayV = "1.3.1"
  val akkaV = "2.3.0"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "io.spray"            %%  "spray-json"    % "1.2.5",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test"
  )
}

seq(Revolver.settings: _*)