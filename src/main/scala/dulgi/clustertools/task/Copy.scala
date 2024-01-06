package dulgi.clustertools.task

import dulgi.clustertools.env.Node
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
class Copy(targetNode: Node, args: Seq[String]) extends Task(targetNode){
  override def taskName: String = s"Copy ${super.taskName}"

  /**
   * check file already exists in target node
   * it will make prompt when file exists in at least once node
   * @return
   */
  def seek(): Boolean = {
    true
  }

  override def execute(): TaskResult = {
    val (sourcePath, destPath) = args match {
      case Seq() => throw new IllegalArgumentException("no args")
      case Seq(arg1: String) => (arg1, arg1)
      case Seq(arg1: String, arg2: String) => (arg1, arg2)
      case Seq(arg1: String, arg2: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
    }

    val sshCommand = Seq("scp", "-P", targetNode.port, sourcePath, s"${targetNode.user}@${targetNode.hostname}:$destPath")
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
