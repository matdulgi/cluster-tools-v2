ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "cluster-tools"
  )

// https://mvnrepository.com/artifact/com.jcraft/jsch
libraryDependencies += "com.jcraft" % "jsch" % "0.1.55"
// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.4.2"
// https://mvnrepository.com/artifact/com.github.pureconfig/pureconfig
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test




