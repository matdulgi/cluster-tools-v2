package dulgi.clustertools.task

import scala.concurrent.Future

sealed abstract class TaskResult(nodeName: String)
case class SequentialTaskResult(nodeName: String, exitCode: Int, stdout: String, stderr: String) extends TaskResult(nodeName)
case class ParallelTaskResult(nodeName: String, future: Future[SequentialTaskResult]) extends TaskResult(nodeName)




