package dulgi.clustertools.task

import dulgi.clustertools.Config
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}

class TestRemoteTask extends AnyFlatSpec with Matchers {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Config.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val fileName = System.getProperty("user.home") + "/test.txt"
  val fileName2 = System.getProperty("user.home") + "/test2.txt"
  val testResult = SequentialTaskResult(testNode.name, 0, "", "")



  val task = new RemoteTask(testNode) {
    override def execute(): TaskResult = {LocalTaskResult(0, "", "")}
    override val command: Seq[String] = Seq.empty
  }

  "remote user" should "be host user under asHostUser option" in {
    val args = Array("ls", "-al")
    val node = testNode.copy(user = "test")
    val hostUser = System.getProperty("user.name")
    val cmd = new Command(node, args,
      asHostUser = true
    )
    assert(cmd.remoteHost.user == hostUser)
  }




  "it" should "not initialize remoteHomePath if path does not contains home path" in {
    val path = "/tmp/example"
    task.replaceRemoteHomePath(path)
  }

  "it" should "initialize remoteHomePath if path contains home path" in {
    val path = s"${System.getProperty("user.home")}/example"
    val resolved = task.replaceRemoteHomePath(path)
    println(resolved)
  }


}
