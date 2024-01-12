package dulgi.clustertools.task

import dulgi.clustertools.{Config, Node}
import java.io.FileNotFoundException
import java.nio.file.{FileSystems, Files, Paths}
import scala.sys.process.ProcessLogger
import scala.sys.process._
import Config.config


class Sync(
            override val targetNode: Node, args: Seq[String],
            convertHomePath: Boolean = config.app.convertHomePath,
            createRemoteDirIfNotExists: Boolean = config.app.createRemoteDirIfNotExists
          ) extends RemoteTask(targetNode){
  val (sourcePath, destPath) = {
    val paths = args match {
      case Seq() => throw new IllegalArgumentException("no args")
      case Seq(arg1: String) => (arg1, arg1)
      case Seq(arg1: String, arg2: String) => (arg1, arg2)
      case Seq(_: String, _: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
    }
    val dotResolved = paths match { case (x, y) => resolveDot(x, y) }
    val homePathResolved = if(convertHomePath)
      dotResolved match {
        case (x, y) => (x, replaceRemoteHomePath(y))
      } else dotResolved

    homePathResolved
  }

//  override val command = Seq("rsync", "-avzhR", "-e", s"'ssh -p ${targetNode.port}'", s"$sourcePath$dirDlm", toRemoteSshPath(destPath))
  override lazy val command = Seq("rsync", "-azvh", "-e", s"'ssh -p ${targetNode.port}'", s"$sourcePath$dirDlm", toRemoteSshPath(destPath))

  private val dirDlm = {
    val path = Paths.get(sourcePath)
    if (Files.exists(path)) {
      if (Files.isDirectory(path)) "/" else ""
    } else throw new FileNotFoundException(s"file not found: $sourcePath")
  }

  override def taskName: String = s"Sync ${super.taskName}"

  def resolveDot(path1: String, path2: String) = {
    List(path1, path2).map{ p =>
      if (p.startsWith(".")){
        val currentDirectory = FileSystems.getDefault.getPath(System.getProperty("user.dir"))
        val resolvedPath = Paths.get(p).normalize()
        val replacedPath = currentDirectory.resolve(resolvedPath).normalize()
        replacedPath.toString
      } else p
    } match {
      case List(a, b) => (a, b)
    }
  }

  override def execute(): TaskResult = {
    val (outputBuffer, errorBuffer) = (new StringBuilder, new StringBuilder)
    val logger = ProcessLogger(
      (o: String) => outputBuffer.append(o + "\n"),
      (e: String) => errorBuffer.append(e + "\n")
    )

    val resultCmd = if(createRemoteDirIfNotExists) {
      val p = Paths.get(destPath).getParent.toString
      command match {
        case h:: t=> h :: s"--rsync-path='mkdir -p $p && rsync'" :: t
      }
    } else command

    val startMsg = s"$taskName in ${targetNode.name} [ ${resultCmd.mkString(" ")} ]"
    logger.err(startMsg)

    val exitCode = resultCmd.mkString(" ") ! logger

    SequentialTaskResult(targetNode.name, exitCode, outputBuffer.toString, errorBuffer.toString)
  }

}


