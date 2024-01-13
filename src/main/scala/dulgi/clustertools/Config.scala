package dulgi.clustertools

import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object Config{
  implicit def camelCaseHint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  lazy val config: Config = {
    val config = getConfigOrThrow[Config]
    config
  }

  def getConfigOrThrow[T: ClassTag : ConfigReader](implicit productHint: ProductHint[T]): T =
    ConfigSource.default.loadOrThrow[T]

  def getConfigOrThrow[T: ClassTag : ConfigReader](path: String)(implicit productHint: ProductHint[T]): T =
    ConfigSource.file(path).loadOrThrow[T]

  def getConfigOrThrowOnDemand(path: String): Config = {
    implicit def camelCaseHint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    getConfigOrThrow[Config](path)
  }


}
final case class Config(
                       nodes: List[Node],
                       curNode: String,
                       groups: Map[String, List[String]],
                       app: App,
                       )

final case class Node(
                     name: String,
                     hostname: String,
                     port: Int,
                     user: String,
                     )

final case class App(
                    convertHomePath: Boolean,
                    seekInCopy: Boolean,
                    createRemoteDirIfNotExists: Boolean,
                    asHostUser: Boolean
                    )