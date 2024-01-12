package dulgi.clustertools

import dulgi.clustertools.task.{Command, Copy, Help, HelpException, ParallelTaskResult, Parallelize, RemoteTask, SequentialTaskResult, Sync, TaskResult}

import java.lang.IllegalArgumentException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ClusterTools {
  def main(args: Array[String]): Unit = {
//    println("Have a good day")
    bootstrap(args)
  }

  def bootstrap(args: Array[String], configPath: String = ""): Unit = {
    val globalConfig = if(configPath != "") Config.getConfigOrThrowOnDemand(configPath)
    else Config.config

    try {
      run(args, globalConfig)
    } catch {
      case e: IllegalArgumentException =>
        println(e.getMessage)
        Help.rootHelp.help()
      case e: NoSuchElementException =>
        println(e.getMessage)
      case _: HelpException =>
        Help.rootHelp.help()
    }
  }


  def run(args: Array[String], globalConfig: Config): Unit = {
    val (isParCmd, parProcessedArgs) = if(args.length == 0) throw new IllegalArgumentException("no argument")
    else {
      if(args(0) == "par") (true, args.tail)
      else if (args(0) == "help") throw new HelpException
      else (false, args)
    }

    val taskStr = if (parProcessedArgs.length < 1) throw new IllegalArgumentException("no task argument")
    else parProcessedArgs(0)

    val targetGroup = if (parProcessedArgs.length < 2) throw new IllegalArgumentException("no group argument")
    else parProcessedArgs(1)

    val targetHosts = try {
      globalConfig.groups(targetGroup)
    } catch {
      case _ => throw new NoSuchElementException(s"group [ $targetGroup ] does not exist")
    }

    val targetNodes = targetHosts.map(host => globalConfig.nodes.filter(_.name == host)).flatten

    val filteredTargetNodes = taskStr match {
      case "cp" | "sync" => targetNodes.filter(_.name != globalConfig.curNode)
      case _ => targetNodes
    }

    val tasks = filteredTargetNodes.map{ node =>
      (isParCmd, taskStr) match {
        case (false, "cmd") => new Command(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.convertHomePath)
        case (false, "cp") => new Copy(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.convertHomePath)
        case (false, "sync") => new Sync(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.convertHomePath)
        case (true, "cmd") => new Command(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.convertHomePath) with Parallelize
        case (true, "cp") => new Copy(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.convertHomePath) with Parallelize
        case (true, "sync") => new Sync(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.convertHomePath) with Parallelize
        case _ => throw new IllegalArgumentException(s"wrong task: $taskStr")
      }
    }

    if (taskStr == "cp" && globalConfig.app.seekInCopy) seekRemoteFile(tasks)

    tasks.foreach(_.onStart())
    if (!isParCmd){
      tasks.foreach{_.execute() match {
          case r: SequentialTaskResult => r.printFinishInfo()
        }
      }
    } else {
      val fl = tasks.map{_.execute() match {
        case r: ParallelTaskResult => r.future}
      }
      val rl = Await.result(Future.sequence(fl), Duration.Inf)
      rl.foreach(_.printFinishInfo())
    }
    tasks.foreach(_.onFinish())

  }

  /**
   * send ls command to remote server
   * all servers those have that file return code 0
   * @param tasks
   */
  private def seekRemoteFile(tasks: List[RemoteTask]): Unit = {
    val targets = tasks.map { case c: Copy => c }.map(_.seek()).filter(_.exitCode == 0)

    if (targets.nonEmpty) {
      println(s"some nodes has that file already : [ ${targets.map(_.nodeName).mkString(", ")} ]")

      def prompt(): Unit = {
        print("will you overwrite it?: (y/n) ")
        val input = scala.io.StdIn.readLine()
        input match {
          case "y" => ()
          case "n" => System.exit(0)
          case _ => prompt()
        }
      }
      prompt()
    }


  }




}

