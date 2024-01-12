package dulgi.clustertools.task

import dulgi.clustertools.Node
import dulgi.clustertools.Config.config

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}


/**
 * Copy task that copy file(s) from one server to another server
 */
abstract class Copier(
                       targetNode: Node,
                       args: Seq[String],
                       val convertHomePath: Boolean = config.app.convertHomePath,
                       val createRemoteDirIfNotExists: Boolean = config.app.createRemoteDirIfNotExists,
                     ) extends RemoteTask ( targetNode = targetNode ) {
  val (sourcePath, destPath) = {
    val paths = args match {
      case Seq() => throw new IllegalArgumentException("no args")
      case Seq(arg1: String) => (arg1, arg1)
      case Seq(arg1: String, arg2: String) => (arg1, arg2)
      case Seq(_: String, _: String, _*) => throw new IllegalArgumentException(s"more then two args: $args")
    }
    val dotResolved = paths match {
      case (x, y) => resolveDot(x, y)
    }
    val homePathResolved = if (convertHomePath)
      dotResolved match {
        case (x, y) => (x, replaceRemoteHomePath(y))
      } else dotResolved

    homePathResolved
  }

  protected val dirDlm: String = {
    val path = Paths.get(sourcePath)
    if (Files.exists(path)) {
      if (Files.isDirectory(path)) "/" else ""
    } else throw new FileNotFoundException(s"file not found: $sourcePath")
  }


}
