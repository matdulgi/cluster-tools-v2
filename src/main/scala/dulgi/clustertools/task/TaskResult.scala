package dulgi.clustertools.task

import scala.concurrent.Future

sealed abstract class TaskResult
case class LocalTaskResult(exitCode: Int, stdout: String, stderr: String) extends TaskResult
case class SequentialTaskResult(nodeName: String, exitCode: Int, stdout: String, stderr: String) extends TaskResult {
  def printFinishInfo(): Unit = {
    exitCode match {
      case 0 =>
        println(s"task in $nodeName has finished successfully")
        println(stdout)

      case _ =>
        println(s"task Failed with code $exitCode !")
        println(stderr)
    }
  }
}

case class ParallelTaskResult(nodeName: String, future: Future[SequentialTaskResult]) extends TaskResult




