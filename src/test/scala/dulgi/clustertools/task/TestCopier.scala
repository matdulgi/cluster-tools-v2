package dulgi.clustertools.task

import dulgi.clustertools.{Config, Node}
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}


class TestCopier extends AnyFlatSpec with Matchers with BeforeAndAfter {
  val testConfigPath = "./conf/test.conf"
  val testConfig: Config = Config.getConfigOrThrowOnDemand(testConfigPath)
  val testNode: Node = testConfig.nodes.head
  val fileName: String = System.getProperty("user.home") + "/test.txt"
  val fileName2: String = System.getProperty("user.home") + "/test2.txt"
  val testResult: SequentialTaskResult = SequentialTaskResult(testNode.name, 0, "", "")

  before {
    println("create test file")
    val p = Paths.get(fileName)
    if (!Files.exists(p)) Files.createFile(p)
  }

  after {
    println("delete test file")
    Files.deleteIfExists(Paths.get(fileName))
    new Command(testNode, Seq("rm", fileName), true).execute()
  }

  "remote home path" should "be remote path under convertHomePath option" in {
    val args = Array(fileName)
    val task = new Copier(testNode, args, convertHomePath = true) {
      override val command: Seq[String] = Seq.empty
      override def execute(): TaskResult = { testResult }
    }

    assert(!task.remotePath.path.contains(System.getProperty("user.home")))
  }

}



