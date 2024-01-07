package dulgi.clustertools.task

import dulgi.clustertools.env.{Node}

import scala.sys.process._

class Command(targetNode: Node, args: Seq[String], convertHomePath: Boolean) extends RemoteTask(targetNode){
  override def taskName: String = s"Command ${super.taskName}"
  override def execute(): TaskResult = {
    val sshCommand = Seq("ssh", "-p", targetNode.port.toString, s"${targetNode.user}@${targetNode.hostname}") ++ args
    val homePathResolved = if(convertHomePath) sshCommand.map(replaceLocalHomePath(_)) else sshCommand
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val startMsg = s"$taskName in ${targetNode.name} [ ${homePathResolved.mkString(" ")} ]"
    logger.out(startMsg)
    logger.err(startMsg)

    val exitCode = homePathResolved.!(logger)

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

}
