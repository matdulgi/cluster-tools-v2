package dulgi.clustertools.task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Parallelize extends RemoteTask {
  abstract override def taskName: String = s"parallel ${super.taskName}"
  abstract override def execute(): TaskResult = {
    val result = Future {
      val sr = super.execute()
      sr match {
        case a: SequentialTaskResult => a
      }
    }

    ParallelTaskResult(targetNode.name, result)
  }

}
