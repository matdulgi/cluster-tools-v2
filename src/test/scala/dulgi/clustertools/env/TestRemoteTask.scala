package dulgi.clustertools.env

import dulgi.clustertools.task.{SequentialTaskResult, RemoteTask, TaskResult}
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
    val config =  Env.getConfigOrThrowOnDemand(configPath)
    val node = config.nodes(0)

    val result = RemoteTask.getRemoteHomePath(node.port, node.user, node.hostname)
    println(result)
    assert(result == s"/home/${node.user}")
  }

  "home path in ssh path" should "replaced with dest system path" in {
    val configPath = "./conf/test.conf"
    val config =  Env.getConfigOrThrowOnDemand(configPath)
    val node = config.nodes(0)

    val task = new RemoteTask(node) {
      override def execute(): TaskResult = SequentialTaskResult("test", 0, "", "")
    }

    val resolvedPath = task.remoteHomePath
    assert( s"/home/${node.user}" == resolvedPath)
  }

}
