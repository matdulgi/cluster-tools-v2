package dulgi.clustertools.task

import scala.concurrent.Future

sealed abstract class TaskResult
case class LocalTaskResult(exitCode: Int, stdout: String, stderr: String) extends TaskResult
case class SequentialTaskResult(nodeName: String, exitCode: Int, stdout: String, stderr: String) extends TaskResult {
  def printFinishInfo(): Unit = {
    exitCode match {
      case 0 =>
        Console.err.println(s"task in $nodeName finished successfully : ")
        Console.err.println(stderr.trim)
        println(stdout.trim)
      case _ =>
        Console.err.println(s"task in $nodeName Failed with code $exitCode : ")
        Console.err.println(stderr.trim)
    }
  }
}

case class ParallelTaskResult(nodeName: String, future: Future[SequentialTaskResult]) extends TaskResult




