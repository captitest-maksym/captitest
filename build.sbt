name := "captitest"

version := "1.0"

scalaVersion := "2.10.6"

libraryDependencies += "com.google.guava" % "guava" % "19.0"

libraryDependencies += "junit" % "junit" % "4.12" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

mainClass in (Compile, run) := Some("captify.test.java.SparseIteratorsApp")
