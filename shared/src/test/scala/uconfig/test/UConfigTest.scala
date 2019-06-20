package uconfig.test

import uconfig.UConfig
import utest._

object UConfigTest extends TestSuite {
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
      val config: UConfig = UConfig(configString)
      config.getInt("obj.int") ==> 42
      config.getLong("obj.long") ==> -123456789123456789L
      config.getBoolean("obj.bool") ==> true
      config.getDouble("obj.sub.double") ==> 123.456
      config.getString("obj.string") ==> "Hello world"
    }
  }
}
