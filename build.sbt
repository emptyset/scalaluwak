import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "LuwakDemo",
    libraryDependencies ++= Seq(
        scalaTest % Test,
        "org.slf4j" % "slf4j-api" % "1.7.5",
        "org.slf4j" % "slf4j-simple" % "1.7.5",
        "com.github.flaxsearch" % "luwak" % "1.4.0",
        "org.apache.lucene" % "lucene-core" % "4.3.0"
    )
  )
