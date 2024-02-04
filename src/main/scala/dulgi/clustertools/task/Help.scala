package dulgi.clustertools.task

object Help{
  val rootHelp = Help(Array.empty)

}

case class HelpException() extends RuntimeException

case class Help(args: Array[String]) extends Task {
  def help(): Unit = {
    execute() match {
      case r: LocalTaskResult => print(r.stdout)
    }
  }

  def execute(): TaskResult = {
    val output = args match {
      case Array() =>
        """
          |Cluster Tools - matdulgi
          |
          |# Usage
          |cltls <task> <group> [arguments]
          |
          |# Task
          |( cmd | cp | sync | pcmd | pcp | psync )
          |
          |# Examples
          |- cmd      ex ) cltls cmd all mkdir /opt/myfile/tmp
          |- cp       ex ) cltls cp /home/dulgi/tmp
          |                cltls cp /home/dulgi/tmp /home/dulgi/tmp
          |- sync     ex ) cltls sync .
          |                cltls sync /home/dulgi/tmp /home/dulgi/tmp
          |- pcmd     ex ) cltls pcmd all mkdir /opt/myfile/tmp
          |- pcp      ex ) cltls pcp /home/dulgi/tmp
          |                cltls pcp /home/dulgi/tmp /home/dulgi/tmp
          |- psync    ex ) cltls psync .
          |                cltls psync /home/dulgi/tmp /home/dulgi/tmp
          |- help     ex ) cltls help
          |""".stripMargin
      case Array(args1) => "developing"
    }

    LocalTaskResult(0, output, "")
  }
}
