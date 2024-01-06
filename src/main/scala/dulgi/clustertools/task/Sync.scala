package dulgi.clustertools.task

import dulgi.clustertools.env.Node
import shapeless.Path

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}
import scala.sys.process.ProcessLogger
import scala.sys.process._


class Sync(override val targetNode: Node, args: Seq[String], replaceHome: Boolean) extends Task(targetNode){
  override def taskName: String = s"Sync ${super.taskName}"

  override def execute(): TaskResult = {
    val (sourcePath, destPath) = args match {
      case Seq() => throw new IllegalArgumentException("no args")
      case Seq(arg1: String) => (arg1, arg1)
      case Seq(arg1: String, arg2: String) => (arg1,
        s"$arg2/${Paths.get(arg1).getFileName.toString}"
      )
      case Seq(arg1: String, arg2: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
    }

    val dirDlm = {
      val tildeResolvedPath = resolveTilde(sourcePath)
      val path = Paths.get(tildeResolvedPath)
      if (Files.exists(path)) {
        if (Files.isDirectory(path)) "/"
        else ""
      }
      else throw new FileNotFoundException(s"file not found: $tildeResolvedPath")
    }

    val sshCommand = Seq("rsync", "-avzh", "-e", s"'ssh -p ${targetNode.port}'", s"$sourcePath$dirDlm", s"${targetNode.user}@${targetNode.hostname}:$destPath").map(resolveTilde(_)).map(resolveTildeInSshPath(_, replaceHome))
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    logger.out(s"$taskName in ${targetNode.name} [ ${sshCommand.mkString(" ")} ]")
    logger.err(s"$taskName in ${targetNode.name} [ ${sshCommand.mkString(" ")} ]")
    val exitCode = sshCommand.mkString(" ") ! logger

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

  override def onStart(): Unit = println()
  override def onFinish(): Unit = println()

}


