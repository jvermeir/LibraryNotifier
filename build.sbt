name := "libraryNotifier"

version := "1.0"

scalaVersion := "2.10.1"

resolvers ++= Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Maven central" at "http://repo1.maven.org/‎",
  "Spray repo" at "http://repo.spray.io",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.1",
  "commons-io" % "commons-io" % "2.4",
  "commons-logging" % "commons-logging" % "1.1.1",
  "commons-codec" % "commons-codec" % "1.6",
  "log4j" % "log4j" % "1.2.17",
  "org.apache.httpcomponents" % "httpclient" % "4.2.5",
  "org.apache.httpcomponents" % "httpclient-cache" % "4.2.5",
  "org.apache.httpcomponents" % "httpcore" % "4.2.5",
  "org.apache.httpcomponents" % "httpmime" % "4.2.5",
  "org.apache.httpcomponents" % "fluent-hc" % "4.2.5",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "org.specs2" %% "specs2" % "1.14" % "test",
  "junit" % "junit" % "4.8.2" % "test",
  "com.typesafe" % "config" % "1.0.2",
  "io.spray" % "spray-can" % "1.1.1",
  "io.spray" % "spray-routing" % "1.1.1",
  "io.spray" % "spray-testkit" % "1.1.1",
  "com.typesafe.akka" %% "akka-actor" % "2.1.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.1.4"
)
