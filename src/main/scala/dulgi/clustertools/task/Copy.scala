package dulgi.clustertools.task

import dulgi.clustertools.env.Node

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
 * @param targetNode
 * @param args
 */
class Copy(targetNode: Node, args: Seq[String], replaceHome: Boolean) extends RemoteTask(targetNode){
  override def taskName: String = s"Copy ${super.taskName}"

  /**
   * check file already exists in target node
   * it will make prompt when file exists in at least once node
   * @return
   */
  def seek(remotePath: String): Boolean = {
    false
  }

  override def execute(): TaskResult = {
    val (sourcePath, destPath) = args match {
      case Seq() => throw new IllegalArgumentException("no args")
      case Seq(arg1: String) => (arg1, arg1)
      case Seq(arg1: String, arg2: String) => (arg1,
        s"$arg2/${Paths.get(arg1).getFileName.toString}"
      )
      case Seq(arg1: String, arg2: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
    }

    // seek function in here

    val sshCommand = Seq("scp", "-P", targetNode.port.toString, sourcePath, s"${targetNode.user}@${targetNode.hostname}:$destPath").map(resolveTilde(_)).map(resolveTildeInSshPath(_, replaceHome))
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    logger.out(s"$taskName in ${targetNode.name} [ ${sshCommand.mkString(" ")} ]")
    logger.err(s"$taskName in ${targetNode.name} [ ${sshCommand.mkString(" ")} ]")
    val exitCode = sshCommand ! logger

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }
}
