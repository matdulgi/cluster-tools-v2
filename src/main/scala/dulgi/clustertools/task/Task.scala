package dulgi.clustertools.task

import scala.language.{postfixOps, reflectiveCalls}


/**
 * Task
 */
abstract class Task {
  def execute(): TaskResult
  def onStart(): Unit = {}
  def onFinish(): Unit = {}

}

