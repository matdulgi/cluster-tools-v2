package dulgi.clustertools.task

import dulgi.clustertools.{Config, Node}
import java.io.FileNotFoundException
import java.nio.file.{FileSystems, Files, Paths}
import scala.sys.process.ProcessLogger
import scala.sys.process._
import Config.config


class Sync(
            override val targetNode: Node,
            args: Seq[String],
            convertHomePath: Boolean = config.app.convertHomePath,
            createRemoteDirIfNotExists: Boolean = config.app.createRemoteDirIfNotExists
          )
  extends Copier(
    targetNode,
    args,
    convertHomePath = convertHomePath,
    createRemoteDirIfNotExists = createRemoteDirIfNotExists
  ){

  override lazy val command: Seq[String] = {
    val base = Seq("rsync", "-azvh", "-e", s"'ssh -p ${targetNode.port.toString}'", s"$sourcePath$dirDlm", remotePath.toString)
    if(createRemoteDirIfNotExists) {
      val p = Paths.get(remotePath.path).getParent.toString
      base match {
        case h :: t => h :: s"--rsync-path='mkdir -p $p && rsync'" :: t
      }
    } else base
  }

  override def taskName: String = s"Sync ${super.taskName}"


  override def execute(): TaskResult = {
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val startMsg = s"$taskName in ${targetNode.name} [ ${command.mkString(" ")} ]"
    logger.err(startMsg)

    val exitCode = command.mkString(" ") ! logger

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

}


