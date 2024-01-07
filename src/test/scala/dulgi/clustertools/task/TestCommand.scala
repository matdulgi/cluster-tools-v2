package dulgi.clustertools.task

import dulgi.clustertools.env.Env
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TestCommand extends AnyFlatSpec with Matchers {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)


  "command" should "ends with code 0" in {
    val args = Array("ls", "-al")
    val cmd = new Command(testNode, args, false)
    val result = cmd.execute()
    val r = result match { case r: SequentialTaskResult => r }

    r.exitCode should be (0)
    r.stdout should not be ("")
  }

  "command with tilde(~)" should "ends with code 0" in {
    val args = Array("ls", "-al", "~")
    val cmd = new Command(testNode, args, true)
    val result = cmd.execute()
    val r = result match { case r: SequentialTaskResult => r }

    r.exitCode should be (0)
    r.stdout should not be ("")
  }

  "parallel command" should "ends with code 0" in {
    val args = Array("ls", "-al", "~")
    val cmd = new Command(testNode, args, true) with Parallelize
    val result = cmd.execute()
    val r = result match { case r: ParallelTaskResult => r }
    val f = r.future
    Await.result(f, 10.seconds)

    val rr = f.value.get.get

    rr.exitCode should be (0)
    rr.stdout should not be ("")
  }

}
