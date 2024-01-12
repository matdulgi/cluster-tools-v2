package dulgi.clustertools

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigSource}


class TestConfig extends AnyFlatSpec with Matchers {

  behavior of "Config"

  "config" should "works with external source" in {
    val file = "./conf/application.conf"
    val source = ConfigSource.file(file)

    import pureconfig.generic.auto._

    implicit def camelCaseHint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
    val config = source.loadOrThrow[Config]
    ()
  }

}
