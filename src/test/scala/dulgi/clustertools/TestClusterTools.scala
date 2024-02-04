package dulgi.clustertools

import dulgi.clustertools.task.HelpException
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Paths}

class TestClusterTools extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Config.getConfigOrThrowOnDemand(testConfigPath)
  val fileName = System.getProperty("user.home") + "/test.txt"
  val fileName2 = System.getProperty("user.home") + "/test2.txt"

  before {
    try {
      println("create test file")
      Files.createFile(Paths.get(fileName))
    } catch {
      case e => println(e.getMessage)
    }
  }

//  after {
//    println("delete test file")
//    Files.deleteIfExists(Paths.get(fileName))
//    new Command(testNode, Seq("rm", fileName), true).execute()
//  }


  "run with help task" should "throws HelpException" in {
    val args = Array("help")
    assertThrows[HelpException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "run with no task arg" should "ends with IllegalArgumentException" in {
    val args = Array.empty[String]
    assertThrows[IllegalArgumentException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "run with no group arg" should "ends with IllegalArgumentException" in {
    val args = Array("cmd")
    assertThrows[IllegalArgumentException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "run with no args for task" should "ends with IllegalArgumentException" in {
    val args = Array("cmd", "all")
    assertThrows[IllegalArgumentException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "run with wrong task arg" should "ends with IllegalArgumentException" in {
    val args = Array("cms", "all", "ls")
    assertThrows[IllegalArgumentException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "command" should "works" in {
    val args = Array("cmd", "all", "ls", "-al")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "command" should "ends with exception with wrong group key" in {
    val args = Array("cmd", "ksofije", "ls", "-al")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "command with home path" should "works in remote servers" in {
    val args = Array("cmd", "all", "ls", "-al", "~")
    ClusterTools.run(args, testConfig)
  }

  "parallel command" should "works" in {
    val args = Array("pcmd", "all", "ls", "-al")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "copy with source path" should "works" in {
    val args = Array("cp", "all", fileName)

    val in = new ByteArrayInputStream("y".getBytes)
    Console.withIn(in) {
      ClusterTools.bootstrap(args, testConfigPath)
    }
  }

  it should "make prompt when file already exists finish with code 0" in {
    val args = Array("cp", "all", fileName)

    val in = new ByteArrayInputStream("y".getBytes)
    Console.withIn(in) {
      ClusterTools.bootstrap(args, testConfigPath)
    }
  }

  "copy with source and dest path" should "works" in {
    val args = Array("cp", "all", fileName, fileName2)

    val in = new ByteArrayInputStream("y".getBytes)
    Console.withIn(in) {
      ClusterTools.bootstrap(args, testConfigPath)
    }
  }

  "parallel copy" should "works" in {
    val args = Array("pcp", "all", fileName)

    val in = new ByteArrayInputStream("y".getBytes)
    Console.withIn(in) {
      ClusterTools.bootstrap(args, testConfigPath)
    }
  }

  "sync with only source path" should "works" in {
    val args = Array("sync", "all", fileName)
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "sync with with source and dest path" should "works" in {
    val args = Array("sync", "all", fileName, "~")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "parallel sync" should "works" in {
    val args = Array("psync", "all", fileName)
    ClusterTools.bootstrap(args, testConfigPath)
  }
}
