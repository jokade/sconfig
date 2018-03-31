package sconfig.test

import sconfig.SConfig
import utest._

object SConfigTest extends TestSuite {
  val configString =
    s"""# this is a comment
       |obj.int = 42
       |obj.long = -123456789123456789
       |
       |// another comment
       |obj {
       |  bool = true
       |  string = "Hello world"
       |
       |  sub {
       |    double = 123.456
       |  }
       |
       |  lists {
       |    int = [1, 2, 3]
       |  }
       |}
     """.stripMargin

  val tests = Tests {
    'config-{
      val config: SConfig = SConfig(configString)
      config.getInt("obj.int") ==> 42
      config.getLong("obj.long") ==> -123456789123456789L
      config.getBoolean("obj.bool") ==> true
      config.getDouble("obj.sub.double") ==> 123.456
      config.
      config.getString("obj.string") ==> "Hello world"
    }
  }
}
