package dulgi.clustertools

import dulgi.clustertools.env.{Config, Env}
import dulgi.clustertools.task.{Command, Copy, Help, HelpException, ParallelTaskResult, Parallelize, SequentialTaskResult, Sync, TaskResult}

import java.lang.IllegalArgumentException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ClusterTools {
  def main(args: Array[String]): Unit = {
    bootstrap(args)
  }

  def bootstrap(args: Array[String], configPath: String = ""): Unit = {
    val globalConfig = if(configPath != "") Env.getConfigOrThrowOnDemand(configPath) else Env.config
    try {
      run(args, globalConfig)
    } catch {
      case e: IllegalArgumentException =>
        println(e.getMessage)
        Help.rootHelp.help()
      case e: HelpException =>
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
      case _ => throw new IllegalArgumentException(s"group $targetGroup isn't specified")
    }

    val targetNodes = targetHosts.map(host => globalConfig.nodes.filter(_.name == host)).flatten

    val filteredTargetNodes = taskStr match {
      case "cp" | "sync" => targetNodes.filter(_.name != globalConfig.curNode)
      case _ => targetNodes
    }

    val tasks = filteredTargetNodes.map{ node =>
      (isParCmd, taskStr) match {
        case (false, "cmd") => new Command(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        case (false, "cp") => new Copy(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.replaceHome)
        case (false, "sync") => new Sync(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.replaceHome)
        case (true, "cmd") => new Command(node, parProcessedArgs.slice(2, parProcessedArgs.length)) with Parallelize
        case (true, "cp") => new Copy(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.replaceHome) with Parallelize
        case (true, "sync") => new Sync(node, parProcessedArgs.slice(2, parProcessedArgs.length), globalConfig.app.replaceHome) with Parallelize
      }
    }

    tasks.foreach(_.onStart())
    val rs = tasks.map(_.execute())

    rs match {
      case (sr: SequentialTaskResult) :: _ =>
        rs.map(_.asInstanceOf[SequentialTaskResult])
          .foreach(_.printFinishInfo())

      case (pr: ParallelTaskResult) :: _ =>
        val pl = rs.map(_.asInstanceOf[ParallelTaskResult])
        val fl =pl.map(_.future)
        val rl = Await.result(Future.sequence(fl), Duration.Inf)

        rl.foreach(_.printFinishInfo())
    }
  }




}

