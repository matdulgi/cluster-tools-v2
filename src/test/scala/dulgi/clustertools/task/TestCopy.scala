package dulgi.clustertools.task

import dulgi.clustertools.env.Env
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.reflect.internal.util.FileUtils
import scala.util.Using

class TestCopy extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val fileName = System.getProperty("user.home") + "/test.txt"
  val fileName2 = System.getProperty("user.home") + "/test2.txt"

  before {
    println("create test file")
    Using( new PrintWriter(fileName) ) { writer =>
      writer.println("good day")
      writer.close()
    }
  }

  after {
    println("delete test file")
    try {
      Files.deleteIfExists(Paths.get(fileName))
    } catch {
      case e: Exception =>
        println(s"Error removing the file: ${e.getMessage}")
    }
    new Command(testNode, Seq("rm", "~/test.txt"), true).execute()
  }

  "copy with source path" should "finish with code 0" in {
    val args = Array(fileName)
    val copy = new Copy(testNode, args, true)
    val result = copy.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ("")
  }

  "copy with source and dest path" should "finish with code 0" in {
    val args = Array(fileName, fileName2)
    val copy = new Copy(testNode, args, true)
    val result = copy.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ("")
  }

  "parallel copy" should "finish with code 0" in {
    val args = Array("~/test.txt")

    val copy = new Copy(testNode, args, true) with Parallelize
    val result = copy.execute()
    val r = result match {
      case r: ParallelTaskResult => r
    }
    val f = r.future
    Await.result(f, 10.seconds)

    val rr = f.value.get.get

    rr.exitCode should be(0)
    rr.stdout should not be ("")
  }

}
