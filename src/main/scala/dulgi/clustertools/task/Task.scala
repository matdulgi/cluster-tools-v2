package dulgi.clustertools.task

import dulgi.clustertools.Node
import dulgi.clustertools.task.RemoteTask.getRemoteHomePath

import java.nio.file.{FileSystems, Paths}
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
 */
object RemoteTask {
  def getRemoteHomePath(port: Int, user: String, hostname: String): String = {
    import scala.sys.process._
//    println(s"getting remote home path for $user@$hostname:$port... in")
    val homePath = (s"ssh -p $port $user@$hostname" + " 'echo $HOME'").!!.trim
    homePath
  }

  case class RemotePath(
                         user: String,
                         hostname: String,
                         path: String
                       ) {
    val remoteHost = s"$user@$hostname"
    val remotePath = s"$remoteHost:$path"
  }

}

abstract class RemoteTask(val targetNode: Node) extends Task {
  def taskName: String = "task"
  lazy val remoteHomePath: String = getRemoteHomePath(targetNode.port, targetNode.user, targetNode.hostname)
  val command: Seq[String]

  override def execute(): TaskResult

  def resolveDot(path1: String, path2: String): (String, String) = {
    List(path1, path2).map { p =>
      if (p.startsWith(".")) {
        val currentDirectory = FileSystems.getDefault.getPath(System.getProperty("user.dir"))
        val resolvedPath = Paths.get(p).normalize()
        val replacedPath = currentDirectory.resolve(resolvedPath).normalize()
        replacedPath.toString
      } else p
    } match {
      case List(a, b) => (a, b)
    }
  }


  @Deprecated
  def resolveTilde(str: String, convertHomePath: Boolean = false): String = {
    if(convertHomePath) {
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
   */
  protected def toRemoteSshPath(path: String): String =
    s"${targetNode.user}@${targetNode.hostname}:$path"


  /**
   * replace home path in start of string
   *
   * using replaceAllIn of Regex for lazy initialization rather replaceAll in String
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
   */
  def replaceRemoteHomePath(path: String, cache: String = ""): String = {
    val rhp = if (cache == "") remoteHomePath else cache
    val homePath = System.getProperty("user.home")

    val homeInFirstResolved = if(path.startsWith(homePath)) path.replaceFirst(homePath, rhp)
    else path

    val sshPathRegex = "([a-zA-Z0-9_-]+)@([a-zA-Z0-9.-]+):(.*)".r
    val sshFormatPathResolved = sshPathRegex.replaceAllIn(homeInFirstResolved, m => {
      val username = m.group(1)
      val domain = m.group(2)
      val path = m.group(3)
      s"$username@$domain:${ path.replaceFirst(homePath, rhp) }"
    })

    sshFormatPathResolved
  }

}

