package dulgi.clustertools.task

import dulgi.clustertools.env.Node

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Parallelize{
  class ParallelCommand(override val targetNode: Node, args: Seq[String]) extends Command(targetNode, args) with Parallelize
  class ParallelCopy(override val targetNode: Node, args: Seq[String], replaceHome: Boolean) extends Copy(targetNode, args, replaceHome) with Parallelize
  class ParallelSync(override val targetNode: Node, args: Seq[String], replaceHome: Boolean) extends Sync(targetNode, args, replaceHome) with Parallelize
}

trait Parallelize extends Task {
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
