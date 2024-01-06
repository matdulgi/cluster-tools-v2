package dulgi.clustertools

import dulgi.clustertools.env.{Config, Env}
import dulgi.clustertools.task.Parallelize.ParallelCommand
import dulgi.clustertools.task.{Command, ParallelTaskResult, SequentialTaskResult, TaskResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ClusterTools {
  def main(args: Array[String]): Unit = {
  }


  def run(args: Array[String], configPath: String): Unit = {
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

    val tasks = targetNodes.map{ node =>
      (isParCmd, taskStr) match {
        case (false, "cmd") => Command(node, parProcessedArgs.slice(2, parProcessedArgs.length))
        //      case "cp" => new Copy
        //      case "sync" => new Sync
        //      case "init" => new Init
        //      case "install" => new Install
        case (true, "cmd") => new ParallelCommand(node, parProcessedArgs.slice(2, parProcessedArgs.length))
//          case (_, "help") => Help(args)

      }
    }

    val rs = tasks.map(_.execute())

    rs match {
      case (sr: SequentialTaskResult) :: _ =>
        rs.map(_.asInstanceOf[SequentialTaskResult])
          .foreach(sr =>println(s"task in ${sr.nodeName} has finished with ${sr.exitCode}"))

      case (pr: ParallelTaskResult) :: _ =>
        val pl = rs.map(_.asInstanceOf[ParallelTaskResult])
        val fl =pl.map(_.future)
        val rl = Await.result(Future.sequence(fl), Duration.Inf)

        rl.foreach(sr =>println(s"task in ${sr.nodeName} has finished with ${sr.exitCode}"))
    }

  }



}

