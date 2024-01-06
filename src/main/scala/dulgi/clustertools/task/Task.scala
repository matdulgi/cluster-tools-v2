package dulgi.clustertools.task

import dulgi.clustertools.env.Node


abstract class Task(val targetNode: Node) {
  def name = "task"
  def execute(): TaskResult
  def onStart(): Unit
  def onFinish(): Unit
}

