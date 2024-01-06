package dulgi.clustertools

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestClusterTools extends AnyFlatSpec with Matchers {
  "exec with command" should "works" in {
    val args = Array("cmd", "all", "ls", "-al")
    ClusterTools.run(args, "./conf/test.conf")
  }

  "exec with parallel command" should "works" in {
    val args = Array("par", "cmd", "all", "ls -al")
    ClusterTools.run(args, "./conf/test.conf")
  }

}
