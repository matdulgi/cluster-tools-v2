package dulgi.clustertools.task

import dulgi.clustertools.env.{Node}

import scala.sys.process._

class Command(targetNode: Node, args: Seq[String]) extends Task(targetNode){
  override def taskName: String = s"Command ${super.taskName}"
  override def execute(): TaskResult = {
    val sshCommand = Seq("ssh", "-p", targetNode.port, s"${targetNode.user}@${targetNode.hostname}") ++ args
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    logger.out(s"$taskName started in ${targetNode.name} for command: [ ${sshCommand.mkString(" ")} ]")
    logger.err(s"$taskName started in ${targetNode.name} for command: [ ${sshCommand.mkString(" ")} ]")
    val exitCode = sshCommand.!(logger)

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, null)
  }

}
