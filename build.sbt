ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "cluster-tools",
    assembly / mainClass := Some("dulgi.clustertools.ClusterTools"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    }
  )


libraryDependencies += "com.jcraft" % "jsch" % "0.1.55"
libraryDependencies += "com.typesafe" % "config" % "1.4.2"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test




