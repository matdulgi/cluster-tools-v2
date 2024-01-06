package dulgi.clustertools.task

import dulgi.clustertools.env.Node
import scala.sys.process.ProcessLogger
import scala.sys.process._


class Sync(override val targetNode: Node, args: Seq[String]) extends Task(targetNode){
  override def taskName: String = s"Sync ${super.taskName}"

  override def execute(): TaskResult = {
    val (sourcePath, destPath) = args match {
      case Seq() => throw new IllegalArgumentException("no args")
      case Seq(arg1: String) => (arg1, arg1)
      case Seq(arg1: String, arg2: String) => (arg1, arg2)
      case Seq(arg1: String, arg2: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
    }

    val sshCommand = Seq("rsync", "-avh", "-e", s"'ssh -p ${targetNode.port}'", targetNode.port, s"${targetNode.user}@${targetNode.hostname}:$sourcePath", destPath)

    val outputBuffer = new StringBuilder
    val errorBuffer = new StringBuilder

    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val process = sshCommand.run()
    val exitCode = process.exitValue()

    val stdout = outputBuffer.toString()
    val stderr = errorBuffer.toString()

    SequentialTaskResult(targetNode.name, exitCode, stdout, stderr)
  }

  override def onStart(): Unit = println()
  override def onFinish(): Unit = println()

}


