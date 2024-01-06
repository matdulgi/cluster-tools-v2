package dulgi.clustertools

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestClusterTools extends AnyFlatSpec with Matchers {
  "command" should "works" in {
    val args = Array("cmd", "all", "ls", "-al")
    ClusterTools.run(args, "./conf/test.conf")
  }

  "command with ~" should "works" in {
    val args = Array("cmd", "all", "ls", "-al", "~")
    ClusterTools.run(args, "./conf/test.conf")
  }

  "parallel command" should "works" in {
    val args = Array("par", "cmd", "all", "ls", "-al")
    ClusterTools.run(args, "./conf/test.conf")
  }

  "copy with source path only" should "works" in {
    val args = Array("cp", "all", "~/test.txt")
    ClusterTools.run(args, "./conf/test.conf")
  }

  "copy with with source and dest path" should "works" in {
    val args = Array("cp", "all", "~/test.txt", "~")
    ClusterTools.run(args, "./conf/test.conf")
  }

  "parallel copy" should "works" in {
    val args = Array("par", "cp", "all", "~/test.txt")
    ClusterTools.run(args, "./conf/test.conf")
  }

}
