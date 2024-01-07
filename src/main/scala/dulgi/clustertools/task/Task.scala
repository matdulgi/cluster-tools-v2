package dulgi.clustertools.task

import dulgi.clustertools.env.Node
import dulgi.clustertools.task.RemoteTask.getRemoteHomePath

import java.nio.file.Paths
import scala.language.{postfixOps, reflectiveCalls}


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
  def getRemoteHomePath(port: Int, user: String, hostname: String): String = {
    import scala.sys.process._
    println(s"getting remote home path for $user@$hostname:$port... in")
    val homePath = (s"ssh -p $port $user@$hostname" + " 'echo $HOME'").!!.trim
    homePath
  }
}
abstract class RemoteTask(val targetNode: Node) extends Task {
  def taskName: String = "task"
  lazy val remoteHomePath = getRemoteHomePath(targetNode.port, targetNode.user, targetNode.hostname)

  override def execute(): TaskResult

  @Deprecated
  def resolveTilde(str: String, convertHomePath: Boolean = false): String = {
    if(convertHomePath == true) {
      str.replaceAll("^~", RemoteTask.getRemoteHomePath(targetNode.port, targetNode.user, targetNode.hostname))
    } else {
      str.replaceAll("^~", System.getProperty("user.home"))
    }
  }

  @Deprecated
  def resolveTildeInSshPath(str: String, convertHomePath: Boolean): String = {
    val firstTildeResolved = resolveTilde(str)
    if (firstTildeResolved.matches(("([a-zA-Z0-9_-]+)@([a-zA-Z0-9.-]+):~(.*)" r).toString)) {
      firstTildeResolved.replaceFirst(":~",
        s":${
          if (convertHomePath) RemoteTask.getRemoteHomePath(targetNode.port, targetNode.user, targetNode.hostname)
          else System.getProperty("user.home")
        }"
      )
    }
    else str
  }


  /**
   * convert file path as remote ssh path format
   *
   * @param path
   * @return
   */
  protected def toRemoteSshPath(path: String): String =
    s"${targetNode.user}@${targetNode.hostname}:$path"


  /**
   * replace home path in start of string
   *
   * using replaceAllIn of Regex for lazy initialization rather replaceAll in String
   * @param path
   * @return
   */
  def replaceLocalHomePath(path: String): String = {
    val homePath = System.getProperty("user.home")
    val homePathRegex = s"^$homePath"r

    homePathRegex.replaceAllIn(path, m => remoteHomePath)

  }

  /**
   * home path in ssh format
   *
   * using replaceAllIn of Regex for lazy initialization rather replaceAll in String
   * @param path
   * @return
   */
  def replaceRemoteHomePath(path: String, cache: String = ""): String = {
    val homePath = System.getProperty("user.home")

    val rhp = if (cache == "") remoteHomePath else cache

    val sshPathRegex = "([a-zA-Z0-9_-]+)@([a-zA-Z0-9.-]+):(.*)".r
    sshPathRegex.replaceAllIn(path, m => {
      val username = m.group(1)
      val domain = m.group(2)
      val path = m.group(3)
      s"$username@$domain:${
        path.replaceFirst(homePath, rhp)
      }"
    })
  }

}

