//     Project: sconfig
//      Module: shared / test
// Description: Test cases for implementations of SConfigObject
package sconfig.test

import com.typesafe.config.{ConfigObject, ConfigValue}
import sconfig.{PathSeq, SConfigObject, SConfigValue}
import scala.language.implicitConversions

import collection.JavaConverters._
import utest._

trait SConfigObjectTests extends TestSuite {
  def createEUT(pairs: Seq[(PathSeq,SConfigValue)]): SConfigObject

  val simpleObj = createEUT(Seq(
    PathSeq("int") -> 42,
    PathSeq("long") -> 123456789L,
    PathSeq("float") -> 1.234F,
    PathSeq("double") -> 12345.6789D,
    PathSeq("bool") -> true,
    PathSeq("null") -> null,
    PathSeq("string") -> "a simple string"
  ).map(p => (p._1,SConfigValue(p._2))))

  val complexObj = createEUT(Seq(
    PathSeq("obj","int") -> 42,
    PathSeq("obj","sub","double") -> 1234.56789,
    PathSeq("obj","bool") -> true,
    PathSeq("obj","sub","bool") -> false,
    PathSeq("string") -> "a simple string",
    PathSeq("other","obj","a") -> "a",
    PathSeq("other","obj","b") -> "b"
  ).map(p => (p._1,SConfigValue(p._2))))

  val tests = Tests {
    'withFallback_find-{
      val foo_a = SConfigObject.empty.withPathValue( PathSeq("foo","a"), SConfigValue(42) )
      val foo_b = SConfigObject.empty.withPathValue( PathSeq("foo","b"), SConfigValue(43) )
      val foo_c = SConfigObject.empty.withPathValue( PathSeq("foo"),
        SConfigObject.empty
          .withPathValue( PathSeq("c"), SConfigValue(44) )
          .withPathValue( PathSeq("d"), SConfigValue(-1) )
      )
      val foo_d = SConfigObject.empty.withPathValue( PathSeq("foo","d"), SConfigValue(45) )

      assert( SConfigValue(42).withFallback(SConfigValue(-1)).asInt == 42 )
      assert( SConfigValue(1).withFallback(foo_a).asInt == 1 )

      val foo_a_b_c_d: SConfigObject = foo_d.withFallback(foo_c).withFallback(foo_b).withFallback(foo_a).asObject
      assert(
        foo_a_b_c_d.find("foo.a").get.asInt == 42,
        foo_a_b_c_d.find("foo.b").get.asInt == 43,
        foo_a_b_c_d.find("foo.c").get.asInt == 44,
        foo_a_b_c_d.find("foo.d").get.asInt == 45
      )
    }

    'withPathValue_find-{
      val obj = SConfigObject.empty
        .withPathValue(PathSeq("int"),SConfigValue(1))
        .withPathValue(PathSeq("obj","int"),SConfigValue(42))
        .withPathValue(PathSeq("obj","double"), SConfigValue(-1.0))
        .withPathValue(PathSeq("obj"),
          SConfigObject.empty
            .withPathValue(PathSeq("bool"),SConfigValue(true))
            .withPathValue(PathSeq("double"),SConfigValue(43.0))
            .withPathValue(PathSeq("sub"),SConfigValue(1))
        )
        .withPathValue(PathSeq("obj","sub"),
          SConfigObject.empty
            .withPathValue(PathSeq("string"),SConfigValue("string"))
            .withPathValue(PathSeq("int"), SConfigObject.empty )
        )
        .withPathValue(PathSeq("obj","sub","int"),SConfigValue(1234))

      assert(
        obj.find("int").get.asInt == 1,
        obj.find("obj.int").get.asInt == 42,
        obj.find("obj.bool").get.asBoolean == true,
        obj.find("obj.double").get.asDouble == 43.0,
        obj.find("obj.sub.string").get.asString == "string",
        obj.find("obj.sub.int").get.asInt == 1234
      )
    }

    'simpleObj-{
      // test the Typesafe ConfigObject interface
      'ConfigObject-{
        'size-{
          assert(simpleObj.size() == 7)
        }
        'keySet-{
          simpleObj.keySet().asScala ==> Set("int","long","float","double","bool","null","string")
        }
        'values-{
          simpleObj.values().asScala.toSet ==> Seq(42,123456789L,1.234F,12345.6789D,true,null,"a simple string").map(SConfigValue.apply).toSet
        }
        'get-{
          simpleObj.get("int") ==> SConfigValue(42)
          simpleObj.get("long") ==> SConfigValue(123456789L)
          simpleObj.get("float") ==> SConfigValue(1.234F)
          simpleObj.get("double") ==> SConfigValue(12345.6789D)
          simpleObj.get("bool") ==> SConfigValue(true)
          simpleObj.get("null") ==> SConfigValue(null)
          simpleObj.get("string") ==> SConfigValue("a simple string")
          intercept[NoSuchElementException]{ simpleObj.get("none") }
        }
      }
    }

    'complexObj-{
      'size-{
        complexObj.size ==> 3
        complexObj.get("obj").size ==> 3
        complexObj.get("obj").get("sub").size ==> 2
      }
      'keySet-{
        complexObj.keySet().asScala ==> Set("obj","other","string")
        complexObj.get("obj").keySet().asScala ==> Set("int","sub","bool")
        complexObj.get("obj").get("sub").keySet().asScala ==> Set("double","bool")
      }
      'find-{
        assert( complexObj.find(Nil) == Some(complexObj) )
        assert( complexObj.find("obj.int").get.asInt == 42 )
        assert( complexObj.find("obj.foo") == None )
        assert( complexObj.find("obj.sub.double").get.asDouble == 1234.56789 )
      }
    }
  }

  implicit def valueToObject(value: ConfigValue): ConfigObject = value match {
    case o: ConfigObject => o
  }
}


