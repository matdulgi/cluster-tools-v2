package dulgi.clustertools.task

import dulgi.clustertools.env.Env
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.util.Using

class TestFileSync extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val fileName = "~/test.txt"
  val fileNameLocal = fileName.replace("~", System.getProperty("user.home"))

  before {
    println("create test file")
    val filePath = Paths.get(fileNameLocal)
    Files.createFile(filePath)
  }

  after {
    println("delete test file")
    Files.deleteIfExists(Paths.get(fileNameLocal))
    new Command(testNode, Seq("rm", "~/test.txt"), true).execute()
  }

  "sync with only source path" should "finish with code 0" in {
    val args = Array("~/test.txt")
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ("")
  }

  "sync with with source and dest path" should "finish with code 0" in {
    val args = Array("~/test.txt", "~")
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ("")
  }

  "parallel sync" should "finish with code 0" in {
    val args = Array("~/test.txt")

    val sync = new Sync(testNode, args, true) with Parallelize
    val result = sync.execute()
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

class TestDirSync extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig = Env.getConfigOrThrowOnDemand(testConfigPath)
  val testNode = testConfig.nodes(0)
  val dirName = "~/cltlstest"
  val fileName = s"$dirName/test.txt"
  val dirNameLocal = dirName.replace("~", System.getProperty("user.home"))
  val fileNameLocal = s"$dirNameLocal/test.txt"

  before {
    println("create test directory and file")
    val directoryPath = Paths.get(dirNameLocal)
    val filePath = Paths.get(fileNameLocal)

    Files.createDirectories(directoryPath)
    Files.createFile(filePath)
  }

  after {
    println("delete test file")
    val directoryPath = Paths.get(dirNameLocal)
    val filePath = Paths.get(fileNameLocal)

    Files.deleteIfExists(filePath)
    Files.deleteIfExists(directoryPath)

    new Command(testNode, Seq("rm", fileName, ";", "rmdir", dirName), true).execute()
  }

  "sync with only source dir path" should "finish with code 0" in {
    val args = Array(dirName)
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ("")
  }

}
