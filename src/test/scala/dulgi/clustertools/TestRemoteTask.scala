package dulgi.clustertools

import dulgi.clustertools.Config
import dulgi.clustertools.task.{RemoteTask, SequentialTaskResult, TaskResult}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class TestRemoteTask extends AnyFlatSpec with Matchers {
  behavior of "Task"

  /**
   * linux only. assuming target user home path is /home/$USER
   */
  "destHomePath" should "work" in {
    val configPath = "./conf/test.conf"
    val config =  Config.getConfigOrThrowOnDemand(configPath)
    val node = config.nodes(0)

    val result = RemoteTask.getRemoteHomePath(node.port, node.user, node.hostname)
    println(result)
    assert(result == s"/home/${node.user}")
  }

  "home path in ssh path" should "replaced with dest system path" in {
    val configPath = "./conf/test.conf"
    val config =  Config.getConfigOrThrowOnDemand(configPath)
    val node = config.nodes(0)

    val task = new RemoteTask(node) {
      override def execute(): TaskResult = SequentialTaskResult("test", 0, "", "")
      override val command: Seq[String] = Seq.empty
    }

    val resolvedPath = task.remoteHomePath
    assert( s"/home/${node.user}" == resolvedPath)
  }

}
