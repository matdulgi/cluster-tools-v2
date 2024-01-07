package dulgi.clustertools

import dulgi.clustertools.env.Env
import dulgi.clustertools.task.HelpException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestClusterTools extends AnyFlatSpec with Matchers {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)

  "command" should "works" in {
    val args = Array("cmd", "all", "ls", "-al")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "command with quoted command" should "works" in {
    val args = Array("cmd", "all", "'ls -al'")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "command with tilde(~)" should "works" in {
    val args = Array("cmd", "all", "ls", "-al", "~")
    ClusterTools.run(args, testConfig)
  }

  "run with help command" should "throws HelpException" in {
    val args = Array("help")
    assertThrows[HelpException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "run with no command arg" should "ends with IllegalArgumentException" in {
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

  "run with no args for command" should "ends with IllegalArgumentException" in {
    val args = Array("cmd", "all")
    assertThrows[IllegalArgumentException]{
      ClusterTools.run(args, testConfig)
    }
  }

  "parallel command" should "works" in {
    val args = Array("par", "cmd", "all", "ls", "-al")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "copy with only source path" should "works" in {
    val args = Array("cp", "all", "~/test.txt")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "copy with with source and dest path" should "works" in {
    val args = Array("cp", "all", "~/test.txt", "~")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "parallel copy" should "works" in {
    val args = Array("par", "cp", "all", "~/test.txt")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "sync with only source path" should "works" in {
    val args = Array("sync", "all", "~/test.txt")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "sync with with source and dest path" should "works" in {
    val args = Array("sync", "all", "~/test.txt", "~")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "sync directory" should "works" in {
    val args = Array("sync", "all", "~/tmp")
    ClusterTools.bootstrap(args, testConfigPath)
  }

  "parallel sync" should "works" in {
    val args = Array("par", "sync", "all", "~/test.txt")
    ClusterTools.bootstrap(args, testConfigPath)
  }
}
