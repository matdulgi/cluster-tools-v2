package dulgi.clustertools

import dulgi.clustertools.env.{Config, Env}
import dulgi.clustertools.task.Parallelize.{ParallelCommand, ParallelCopy, ParallelSync}
import dulgi.clustertools.task.{Command, Copy, Help, ParallelTaskResult, SequentialTaskResult, Sync, TaskResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ClusterTools {
  def main(args: Array[String]): Unit = {
    run(args)
  }

  def run(args: Array[String], configPath: String = ""): Unit = {
    import pureconfig.generic.ProductHint
    import pureconfig.generic.auto._
    import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader, ConfigSource}
    implicit def camelCaseHint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    val globalConfig = if(configPath != "") Env.getConfigOrThrow[Config](configPath) else Env.config

    val (isParCmd, parProcessedArgs) = if(args(0) == "par") (true, args.tail) else (false, args)

    val taskStr = if (parProcessedArgs.length > 0) parProcessedArgs(0) else {
      throw new IllegalArgumentException("no task argument")
    }

    val targetGroup = if (parProcessedArgs.length > 2) parProcessedArgs(1) else {
      throw new IllegalArgumentException("no group argument")
    }

    val targetHosts = globalConfig.groups(targetGroup)
    val targetNodes = targetHosts.map(host => globalConfig.nodes.filter(_.name == host)).flatten

    val filteredTargetNodes = taskStr match {
      case "cp" | "sync" => targetNodes.filter(_.name != globalConfig.curNode)
      case _ => targetNodes
    }

    val tasks = filteredTargetNodes.map{ node =>
      (isParCmd, taskStr) match {
        case (false, "cmd") => new Command(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        case (false, "cp") => new Copy(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        case (false, "sync") => new Sync(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        case (true, "cmd") => new ParallelCommand(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        case (true, "cp") => new ParallelCopy(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        case (true, "sync") => new ParallelSync(node, parProcessedArgs.slice(2, parProcessedArgs.length))
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

