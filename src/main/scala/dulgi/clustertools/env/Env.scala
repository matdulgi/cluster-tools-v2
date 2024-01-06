package dulgi.clustertools.env

import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object Env {
  implicit def camelCaseHint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  def getConfigOrThrow[T: ClassTag: ConfigReader](implicit productHint: ProductHint[T]): T =
    ConfigSource.default.loadOrThrow[T]

  def getConfigOrThrow[T: ClassTag: ConfigReader](path: String)(implicit productHint: ProductHint[T]): T =
    ConfigSource.file(path).loadOrThrow[T]

  lazy val config: Config = {
    val config = getConfigOrThrow[Config]
    config
  }

}

