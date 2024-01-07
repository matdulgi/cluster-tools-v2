package dulgi.clustertools.task

import dulgi.clustertools.env.Env
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.nio.file.{Files, Paths}
import scala.concurrent.Await


class TestCopy extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val fileName = System.getProperty("user.home") + "/test.txt"
  val fileName2 = System.getProperty("user.home") + "/test2.txt"

  before {
    println("create test file")
    Files.createFile(Paths.get(fileName))
  }

  after {
    println("delete test file")
    Files.deleteIfExists(Paths.get(fileName))
    new Command(testNode, Seq("rm", fileName), true).execute()
  }

  "copy with source path" should "finish with code 0" in {
    val args = Array(fileName)
    val copy = new Copy(testNode, args, true)
    val result = copy.execute()

    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
  }


  "copy with source and dest path" should "finish with code 0" in {
    val args = Array(fileName, fileName2)
    val copy = new Copy(testNode, args, true)
    val result = copy.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
  }

  "parallel copy" should "finish with code 0" in {
    val args = Array(fileName)

    val copy = new Copy(testNode, args, true) with Parallelize
    val result = copy.execute()
    val r = result match {
      case r: ParallelTaskResult => r
    }
    val f = r.future
    Await.result(f, 10.seconds)

    val rr = f.value.get.get

    rr.exitCode should be(0)
    rr.stdout should not be ""
  }

}


class TestDirCopy extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val dirName = s"${System.getProperty("user.home")}/cltlstest"
  val fileName = s"$dirName/test.txt"

  before {
    println("create test directory and file")
    Files.createDirectories(Paths.get(dirName))
    Files.createFile(Paths.get(fileName))
  }

  after {
    println("delete test file")
    Files.deleteIfExists(Paths.get(fileName))
    Files.deleteIfExists(Paths.get(dirName))

    new Command(testNode, Seq("rm", fileName, ";", "rmdir", dirName), true).execute()
  }

  "copy with only source dir path" should "finish with code 0" in {
    val args = Array(dirName)
    val copy = new Copy(testNode, args, true)
    val result = copy.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
  }

}
