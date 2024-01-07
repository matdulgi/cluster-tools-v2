package dulgi.clustertools.task

import dulgi.clustertools.env.Node

import java.io.File
import java.nio.file.Paths
import scala.language.postfixOps
import scala.sys.process.ProcessLogger
import scala.sys.process._

/**
 * Cluster Copy
 *
 * Copy File from Source Node to Destination Nodes
 * File will not be copied when Source node is Destination node
 *
 * input args can be like:
 * - /home/user/examole.txt
 * - /home/user/example.txt /home/user/dir
 *
 * destination path will set to the same path as the local path
 * if there is only local file path argument
 *
 * @param targetNode
 * @param args
 */
class Copy(targetNode: Node, args: Seq[String], convertHomePath: Boolean) extends RemoteTask(targetNode){
  val (sourcePath, destPath) = args match {
    case Seq() => throw new IllegalArgumentException("no args")
    case Seq(arg1: String) => (arg1, arg1)
    case Seq(arg1: String, arg2: String) => (arg1, arg2)
    case Seq(_: String, _: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
  }

  private var _remoteHomePathCache = ""

  override def taskName: String = s"Copy ${super.taskName}"


  /**
   * check if file already exists in target node
   *
   * seek will be executed in multiple nodes if option is set true
   * it will make prompt when file exists in at least once node
   * @return
   */
  def seek(): SequentialTaskResult = {
    val t = new Command(targetNode, Seq("ls", destPath), convertHomePath)
    val r = t.execute()

    // caching
    if(sourcePath.contains(System.getProperty("user.home")))
      this._remoteHomePathCache = t.remoteHomePath

    r match { case r: SequentialTaskResult => r }
  }

  override def execute(): TaskResult = {
    val sshCommand = Seq("scp", "-P", targetNode.port.toString, sourcePath, toRemoteSshPath(destPath))
    val homePathResolved = if(convertHomePath) sshCommand.map(replaceRemoteHomePath(_, _remoteHomePathCache)) else sshCommand
    val recursiveOptionResolved = if (new File(sourcePath).isDirectory)
      homePathResolved.head +: "-r" +: homePathResolved.tail
    else homePathResolved

    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val startMsg = s"$taskName in ${targetNode.name} [ ${recursiveOptionResolved.mkString(" ")} ]"
    logger.err(startMsg)

    val exitCode = recursiveOptionResolved ! logger

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }
}
