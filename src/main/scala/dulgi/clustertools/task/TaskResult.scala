package dulgi.clustertools.task

import scala.concurrent.Future

sealed abstract class TaskResult(nodeName: String)
case class SequentialTaskResult(nodeName: String, exitCode: Int, stdout: String, stderr: String) extends TaskResult(nodeName){
  def printFinishInfo(): Unit = {
    exitCode match {
      case 0 =>
        println(s"task in ${nodeName} has finished ")

        println(stdout)

      case _ =>
        println(s"task Failed with exitcode ${exitCode}")
        println(stderr)
    }
  }


}
case class ParallelTaskResult(nodeName: String, future: Future[SequentialTaskResult]) extends TaskResult(nodeName)




