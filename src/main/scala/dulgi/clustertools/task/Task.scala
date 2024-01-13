package dulgi.clustertools.task

import dulgi.clustertools.{Config, Node}
import dulgi.clustertools.task.RemoteTask.{RemoteHost, SshURL}

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
  case class SshURL(
                   user: String,
                   hostname: String,
                   path: String
                   ) {
    val remoteHost = RemoteHost(user, hostname)
    override def toString: String = s"$remoteHost:$path"
  }

  case class RemoteHost(
                       user: String,
                       hostname: String
                      ){
    override def toString: String = s"$user@$hostname"
  }

}

abstract class RemoteTask(
                         val targetNode: Node,
                         val asHostUser: Boolean = Config.config.app.asHostUser

                         ) extends Task {
  def taskName: String = "task"

  lazy val remoteHomePath: String = {
    import scala.sys.process._
    //    println(s"getting remote home path for $user@$hostname:$port... in")
    val homePath = (s"ssh -p ${targetNode.port} ${remoteHost.toString}" + " 'echo $HOME'").!!.trim
    homePath
  }

  val command: Seq[String]

  val remoteHost = {
    val remoteUser = if(asHostUser) System.getProperty("user.name") else targetNode.user
    RemoteHost(remoteUser, targetNode.hostname)
  }

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
  def resolveTilde(str: String): String = "^~".r.replaceFirstIn(str, remoteHomePath)

  @Deprecated
  def resolveTildeInSshPath(sshPath: SshURL): SshURL = {
    sshPath.copy(path = resolveTilde(sshPath.path))
  }


  /**
   * convert file path as remote ssh path format
   * responsible to convert username, home path, and so on as configurations
   */
  protected def toRemoteSshURL(
                              path: String,
                              ): SshURL = {
    val targetUser = if(asHostUser) System.getProperty("user.name") else targetNode.user
    SshURL(targetUser, targetNode.hostname, path)
  }


  /**
   * replace home path in start of string
   *
   * using replaceAllIn of Regex for lazy initialization rather replaceAll in String
   */
  def replaceLocalHomePath(path: String): String = {
    val homePath = System.getProperty("user.home")
    val homePathRegex = s"^$homePath"r

    homePathRegex.replaceAllIn(path, _ => remoteHomePath)

  }


  /**
   * home path in ssh format
   * remote path can be path syntax or ssh url syntax
   *
   * using replaceAllIn of Regex for lazy initialization rather replaceAll in String
   */
  def replaceRemoteHomePath(path: String, cache: String = ""): String = {
    val _remoteHomePath = if (cache == "") remoteHomePath else cache
    val hostHomePath = System.getProperty("user.home")

    val homeInFirstResolved = if(path.startsWith(hostHomePath)) path.replaceFirst(hostHomePath, _remoteHomePath)
    else path

    val sshPathRegex = "([a-zA-Z0-9_-]+)@([a-zA-Z0-9.-]+):(.*)".r
    val sshFormatPathResolved = sshPathRegex.replaceAllIn(homeInFirstResolved, m => {
      val username = m.group(1)
      val domain = m.group(2)
      val path = m.group(3)
      s"$username@$domain:${ path.replaceFirst(hostHomePath, _remoteHomePath) }"
    })

    sshFormatPathResolved
  }

}

