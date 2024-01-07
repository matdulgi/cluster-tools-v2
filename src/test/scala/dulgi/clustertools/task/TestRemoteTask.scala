package dulgi.clustertools.task

import dulgi.clustertools.env.Env
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayOutputStream, PrintStream}

class TestRemoteTask extends AnyFlatSpec with Matchers {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val fileName = System.getProperty("user.home") + "/test.txt"
  val fileName2 = System.getProperty("user.home") + "/test2.txt"


  val task = new RemoteTask(testNode) {
    override def execute(): TaskResult = {LocalTaskResult(0, "", "")}
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
