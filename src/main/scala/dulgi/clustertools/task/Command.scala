package dulgi.clustertools.task

import dulgi.clustertools.env.{Node}

import scala.sys.process._

class Command(targetNode: Node, args: Seq[String]) extends RemoteTask(targetNode){
  override def taskName: String = s"Command ${super.taskName}"
  override def execute(): TaskResult = {
    val sshCommand = (Seq("ssh", "-p", targetNode.port.toString, s"${targetNode.user}@${targetNode.hostname}") ++ args)
    val tildeResolved = sshCommand.map(resolveTilde(_))
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val startMsg = s"$taskName in ${targetNode.name} [ ${sshCommand.mkString(" ")} ]"
    logger.out(startMsg)
    logger.err(startMsg)

    val exitCode = sshCommand.!(logger)

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

}
