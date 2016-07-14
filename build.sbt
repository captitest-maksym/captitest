name := "captitest"

version := "1.0"

scalaVersion := "2.10.6"

libraryDependencies += "com.google.guava" % "guava" % "19.0"

mainClass in (Compile, run) := Some("captify.test.java.SparseIteratorsApp")
