package dulgi.clustertools.task

import dulgi.clustertools.env.Node
import shapeless.Path

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}
import scala.sys.process.ProcessLogger
import scala.sys.process._


class Sync(override val targetNode: Node, args: Seq[String], convertHomePath: Boolean) extends RemoteTask(targetNode){
  val (sourcePath, destPath) = args match {
    case Seq() => throw new IllegalArgumentException("no args")
    case Seq(arg1: String) => (arg1, arg1)
    case Seq(arg1: String, arg2: String) => (arg1, arg2)
    case Seq(_: String, _: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
  }

  private val dirDlm = {
    val path = Paths.get(sourcePath)
    if (Files.exists(path)) {
      if (Files.isDirectory(path)) "/"
      else ""
    }
    else throw new FileNotFoundException(s"file not found: $sourcePath")
  }

  override def taskName: String = s"Sync ${super.taskName}"

  override def execute(): TaskResult = {
    val sshCommand = Seq("rsync", "-avzh", "-e", s"'ssh -p ${targetNode.port}'", s"$sourcePath$dirDlm", toRemoteSshPath(destPath))
    val homePathResolved = if(convertHomePath) sshCommand.map(replaceRemoteHomePath(_)) else sshCommand


    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val startMsg = s"$taskName in ${targetNode.name} [ ${homePathResolved.mkString(" ")} ]"
    logger.err(startMsg)

    val exitCode = homePathResolved.mkString(" ") ! logger

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

}


