name := "libraryNotifier"

version := "1.0"

scalaVersion := "2.10.0"

resolvers ++= Seq ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                   "Maven central" at "http://repo1.maven.org/‎",
                   "Spray repo" at "http://repo.spray.io",
                   "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
                   )

scalacOptions ++= Seq ( "-deprecation", "-feature" )

libraryDependencies ++= Seq (
   "org.scala-lang"                          %   "scala-reflect"               % "2.10.1",
   "com.typesafe.akka"                       %%  "akka-actor"                  % "2.1.4" ,
  "com.typesafe.akka"                       %%  "akka-slf4j"                  % "2.1.4"  ,
  "com.typesafe.akka"                       %%  "akka-testkit"                % "2.1.4"  ,
  "org.parboiled"                           %%  "parboiled-scala"             % "1.1.5"  ,
  "com.chuusai"                             %%  "shapeless"                   % "1.2.4"  ,
  "org.scalatest"                           %%  "scalatest"                   % "1.9.1"  ,
  "org.specs2"                              %%  "specs2"                      % "1.14"   ,
  "com.googlecode.concurrentlinkedhashmap"  %   "concurrentlinkedhashmap-lru" % "1.3.2"  ,
  "org.eclipse.jetty"                       %   "jetty-webapp"                % "8.1.10.v20130312",
  "org.eclipse.jetty.orbit"                 %   "javax.servlet"               % "3.0.0.v201112011016" artifacts Artifact("javax.servlet", "jar", "jar"),
  "ch.qos.logback"                          %   "logback-classic"             % "1.0.12",
  "org.jvnet.mimepull"                      %   "mimepull"                    % "1.9.2"  ,
  "org.pegdown"                             %   "pegdown"                     % "1.2.1"   ,
  "net.liftweb"                             %%  "lift-json"                   % "2.5-RC5"  ,
  "org.json4s"                              %%  "json4s-native"               % "3.2.4"     ,
  "org.json4s"                              %%  "json4s-jackson"              % "3.2.4"     ,
  "io.spray"                                %%  "spray-json"                  % "1.2.4"  ,
  "io.spray"                                %%  "twirl-api"                   % "0.6.2" ,
              			"org.scalatest"    % "scalatest_2.10"    % "1.9.1"       ,
              			"junit"     % "junit"      % "4.8.2"       % "test",
"commons-io"       % "commons-io"        % "2.4"
   )