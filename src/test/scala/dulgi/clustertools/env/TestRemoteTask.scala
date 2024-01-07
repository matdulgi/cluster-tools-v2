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

  "tilde in ssh path" should "replaced with dest system path" in {
    val configPath = "./conf/test.conf"
    val config =  Env.getConfigOrThrowOnDemand(configPath)
    val node = config.nodes(0)

    val inputPath = s"${node.user}@${node.hostname}:~/path"

    val task = new RemoteTask(node) {
      override def execute(): TaskResult = SequentialTaskResult("test", 0, "", "")
    }

    val resolvedPath = task.resolveTildeInSshPath(inputPath, true)
    assert( s"${node.user}@${node.hostname}:/home/${node.user}/path" == resolvedPath)

  }



}
