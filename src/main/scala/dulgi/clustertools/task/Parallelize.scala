package dulgi.clustertools.task

import dulgi.clustertools.env.Node

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Parallelize{
  class ParallelCommand(targetNode: Node, args: Seq[String]) extends Command(targetNode, args) with Parallelize
}

trait Parallelize extends Task {
  override def name: String = s"parallel ${super.name}"

  abstract override def execute(): TaskResult = {
    val result = Future {
      val sr = super.execute()
      sr match {
        case a: SequentialTaskResult => a
      }
    }

    ParallelTaskResult(targetNode.name, result)
    //    (0, "", "")

  }

}
