package dulgi.clustertools.task

import dulgi.clustertools.Node
import scala.sys.process._
import dulgi.clustertools.Config.config

class Command(targetNode: Node, args: Seq[String],
              convertHomePath: Boolean = config.app.convertHomePath,
              createRemoteDirIfNotExists: Boolean = config.app.convertHomePath
             ) extends RemoteTask(targetNode){
  override def taskName: String = s"Command ${super.taskName}"

  override val command: Seq[String] = Seq.empty
  override def execute(): TaskResult = {
    if (args.length == 0){
      throw new IllegalArgumentException("no command args")
    }
    val sshCommand = Seq("ssh", "-p", targetNode.port.toString, s"${targetNode.user}@${targetNode.hostname}") ++ args
    val homePathResolved = if(convertHomePath) sshCommand.map(replaceLocalHomePath(_)) else sshCommand
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val startMsg = s"$taskName in ${targetNode.name} [ ${homePathResolved.mkString(" ")} ]"
    logger.err(startMsg)

    val exitCode = homePathResolved.!(logger)

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

}
