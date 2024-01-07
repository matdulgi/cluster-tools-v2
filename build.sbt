ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "cluster-tools",
    assembly / mainClass := Some("dulgi.clustertools.ClusterTools"),
    assembly / version := "0.1.0-SNAPSHOT",
    assembly / assemblyJarName := s"${name.value}-assembly-${version.value}.jar",
    assembly / assemblyOutputPath := file(s"./lib/${(assembly / assemblyJarName).value}"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    }
  )

libraryDependencies += "com.typesafe" % "config" % "1.4.2"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test





