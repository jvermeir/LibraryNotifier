name := "boeken"

version := "1.0"

scalaVersion := "2.10.0"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

scalacOptions ++= Seq ( "-deprecation", "-feature" )

libraryDependencies ++= Seq (
			"org.scalatest" % "scalatest_2.10" % "1.9.1",
			"junit" % "junit" % "4.8.2" % "test",
			"commons-io" % "commons-io" % "2.4"
            )