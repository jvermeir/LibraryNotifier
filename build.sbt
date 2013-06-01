name := "libraryNotifier"

version := "1.0"

scalaVersion := "2.10.0"

resolvers ++= Seq ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                   "Spray repo" at "http://repo.spray.io"
                   )

scalacOptions ++= Seq ( "-deprecation", "-feature" )

libraryDependencies ++= Seq (
			"org.scalatest" % "scalatest_2.10" % "1.9.1",
			"junit" % "junit" % "4.8.2" % "test",
			"commons-io" % "commons-io" % "2.4",
			"io.spray" % "spray-io" % "1.1-M7",
            "io.spray" % "spray-can" % "1.1-M7"
            )