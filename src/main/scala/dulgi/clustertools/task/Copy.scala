package dulgi.clustertools.task

import dulgi.clustertools.Config.config
import dulgi.clustertools.Node

import java.io.File
import java.nio.file.Paths
import scala.language.postfixOps
import scala.sys.process.ProcessLogger
import scala.sys.process._



/**
 * Cluster Copy
 *
 * Copy File from Source Node to Destination Nodes
 * File will not be copied when Source node is Destination node
 *
 * input args can be like:
 * - /home/user/examole.txt
 * - /home/user/example.txt /home/user/dir
 *
 * destination path will set to the same path as the local path
 * if there is only local file path argument
 */
class Copy(
          targetNode: Node, args: Seq[String],
          convertHomePath: Boolean,
          createRemoteDirIfNotExists: Boolean = config.app.createRemoteDirIfNotExists
          )
  extends Copier(
    targetNode,
    args,
    convertHomePath = convertHomePath,
    createRemoteDirIfNotExists = createRemoteDirIfNotExists,
  ) {

  override val command: Seq[String] = {
    val base = Seq("scp", "-P", targetNode.port.toString, sourcePath, remotePath.toString)
    val recursiveOptionResolved = if (new File(sourcePath).isDirectory) {
      base.head +: "-r" +: base.tail
    } else base
    recursiveOptionResolved
  }

  private var _remoteHomePathCache = ""

  override def taskName: String = s"Copy ${super.taskName}"


  /**
   * check if file already exists in target node
   *
   * seek will be executed in multiple nodes if option is set true
   * it will make prompt when file exists in at least once node
   * @return
   */
  def seek(): SequentialTaskResult = {
    val task = new Command(targetNode, Seq("ls", remotePath.toString), convertHomePath)
    val result = task.execute()

    cacheRemotePath(task)

    result match { case r: SequentialTaskResult => r }
  }


  private def createRemoteParentDirectoriesCommand = {
    val parentPath = Paths.get(remotePath.toString).getParent.toString
    val task = new Command(targetNode, Seq("mkdir -p ", parentPath), convertHomePath)
    task.execute()
  }

  /**
   * cache remote path
   * it prevent to connect remote server in both tasks
   * if source path contains user home, the reference to remotePath has already bean occur
   */
  private def cacheRemotePath(task: RemoteTask): Unit = {
    if (sourcePath.contains(System.getProperty("user.home")))
      this._remoteHomePathCache = task.remoteHomePath
  }

  override def execute(): TaskResult = {
    if(createRemoteDirIfNotExists) {
      createRemoteParentDirectoriesCommand
    }

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
