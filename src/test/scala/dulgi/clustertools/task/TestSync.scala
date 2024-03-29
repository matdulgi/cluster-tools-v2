package dulgi.clustertools.task

import dulgi.clustertools.{Config, Node}
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.io.PrintWriter
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import scala.concurrent.Await
import scala.util.Using

class TestFileSync extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig: Config = Config.getConfigOrThrowOnDemand(testConfigPath)
  val testNode: Node = testConfig.nodes.head
  val fileName = s"${System.getProperty("user.home")}/test.txt"

  before {
    println("create test file")
    val p = Paths.get(fileName)
    if(!Files.exists(p)) Files.createFile(p)
  }

  after {
    println("delete test file")
    Files.deleteIfExists(Paths.get(fileName))
    new Command(testNode, Seq("rm", fileName), true).execute()
  }

  "sync with only source path" should "finish with code 0" in {
    val args = Array(fileName)
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
  }

  it should "not check if file is exist without execute" in {
    val args = Array("file_does_not_exist")
    val sync = new Sync(testNode, args, true)
  }

  "sync with only filename" should "resolved to under cwd" in {
    val args = Array("test.txt")
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
  }


  "sync with with source and dest path" should "finish with code 0" in {
    val args = Array(fileName, "~")
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
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
    rr.stdout should not be ""
  }

}

class TestDirSync extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig: Config = Config.getConfigOrThrowOnDemand(testConfigPath)
  val testNode: Node = testConfig.nodes.head
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

  "sync with only source dir path" should "finish with code 0" in {
    val args = Array(dirName)
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    r.exitCode should be(0)
    r.stdout should not be ""
  }


  "sync with dot path" should "sync file into same path in remote server" in {
    System.setProperty("user.dir", dirName)
    val args = Array(".")
    val sync = new Sync(testNode, args, true)
    val result = sync.execute()
    val r = result match {
      case r: SequentialTaskResult => r
    }

    println(r.stdout)
    println(r.stderr)
    r.exitCode should be(0)
    r.stdout should not be ""
  }


}
