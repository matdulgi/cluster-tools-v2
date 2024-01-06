package dulgi.clustertools.task

import com.jcraft.jsch.JSch
import dulgi.clustertools.env.{Env, Node}

import scala.sys.process._

case class Command(override val targetNode: Node, args: Seq[String]) extends Task(targetNode){
  override def name: String = s"${super.name} command"

  override def execute(): TaskResult = {
    val sshCommand = Seq("ssh", "-p", targetNode.port, s"${targetNode.user}@${targetNode.hostname}") ++ args

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
