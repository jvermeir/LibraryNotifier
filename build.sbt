name := "libraryNotifier"

version := "1.0"

scalaVersion := "2.10.1"

resolvers ++= Seq ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                   "Maven central" at "http://repo1.maven.org/‎",
                   "Spray repo" at "http://repo.spray.io",
                   "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
                   )

scalacOptions ++= Seq ( "-deprecation", "-feature" )

libraryDependencies ++= Seq (
  "org.scala-lang"                          %   "scala-reflect"               % "2.10.1",
  "commons-io"                              %   "commons-io"                  % "2.4",
  "org.scalatest"                           %   "scalatest_2.10"              % "1.9.1"       % "test",
  "org.specs2"                              %%  "specs2"                      % "1.14"        % "test",
  "junit"                                   %   "junit"                       % "4.8.2"       % "test",
  "com.typesafe"                            %   "config"                      % "1.0.2"
)

libraryDependencies ++= Seq(
  "io.spray"            %   "spray-can"     % "1.1-M8",
  "io.spray"            %   "spray-routing" % "1.1-M8",
  "io.spray"            %   "spray-testkit" % "1.1-M8",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.1.4",
  "com.typesafe.akka"   %%  "akka-testkit"  % "2.1.4",
  "org.specs2"          %%  "specs2"        % "1.14" % "test"
)
