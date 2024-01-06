package dulgi.clustertools.task

import dulgi.clustertools.env.Node

import java.nio.file.Paths


abstract class Task(val targetNode: Node) {
  def taskName: String = "task"
  def execute(): TaskResult
  def onStart(): Unit = { }
  def onFinish(): Unit = { }
  def resolveTilde(str: String): String = Paths.get(str.replaceFirst("^~", System.getProperty("user.home"))).toString

}

