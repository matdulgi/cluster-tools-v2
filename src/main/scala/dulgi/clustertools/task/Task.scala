package dulgi.clustertools.task

import dulgi.clustertools.env.Node

import java.nio.file.Paths
import scala.language.postfixOps


object Task {
  def destHomePath(port: Int, user: String, hostname: String): String = {
    import scala.sys.process._
    val homePath = (s"ssh -p $port $user@$hostname" + " 'echo $HOME'").!!.trim
    homePath
  }

}
abstract class Task(val targetNode: Node) {
  def taskName: String = "task"
  def execute(): TaskResult
  def onStart(): Unit = { }
  def onFinish(): Unit = { }
  def resolveTilde(str: String): String = {
    str.replaceAll("^~", System.getProperty("user.home"))
  }

  def resolveTildeInSshPath(str: String, replaceHome: Boolean): String = {
    val firstTildeResolved = resolveTilde(str)
    if (firstTildeResolved.matches(("([a-zA-Z0-9_-]+)@([a-zA-Z0-9.-]+):~(.*)" r).toString)) {
      firstTildeResolved.replaceFirst(":~",
        ":" + {
          if (replaceHome) {
            Task.destHomePath(targetNode.port, targetNode.user, targetNode.hostname)
          } else System.getProperty("user.home")
        }
      )
    }
    else str
  }



}

