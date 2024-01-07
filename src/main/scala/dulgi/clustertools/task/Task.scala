package dulgi.clustertools.task

import dulgi.clustertools.env.Node

import java.nio.file.Paths
import scala.language.postfixOps


/**
 * Task
 */
abstract class Task {
  def execute(): TaskResult
  def onStart(): Unit = {}
  def onFinish(): Unit = {}

}

/**
 * Task which access to remote server
 * @param targetNode
 */
object RemoteTask {
  def destHomePath(port: Int, user: String, hostname: String): String = {
    import scala.sys.process._
    val homePath = (s"ssh -p $port $user@$hostname" + " 'echo $HOME'").!!.trim
    homePath
  }
}
abstract class RemoteTask(val targetNode: Node) extends Task {
  def taskName: String = "task"
  override def execute(): TaskResult
  def resolveTilde(str: String): String = {
    str.replaceAll("^~", System.getProperty("user.home"))
  }

  def resolveTildeInSshPath(str: String, replaceHome: Boolean): String = {
    val firstTildeResolved = resolveTilde(str)
    if (firstTildeResolved.matches(("([a-zA-Z0-9_-]+)@([a-zA-Z0-9.-]+):~(.*)" r).toString)) {
      firstTildeResolved.replaceFirst(":~",
        ":" + {
          if (replaceHome) {
            RemoteTask.destHomePath(targetNode.port, targetNode.user, targetNode.hostname)
          } else System.getProperty("user.home")
        }
      )
    }
    else str
  }



}

