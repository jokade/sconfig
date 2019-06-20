package uconfig.test

import com.typesafe.config.{ConfigObject, ConfigValue}
import uconfig.{PathSeq, UConfigObject, UConfigValue}
import scala.language.implicitConversions

import collection.JavaConverters._
import utest._


trait UConfigObjectTests extends TestSuite {
  def createEUT(pairs: Seq[(PathSeq,UConfigValue)]): UConfigObject

  val simpleObj = createEUT(Seq(
    PathSeq("int") -> 42,
    PathSeq("long") -> 123456789L,
    PathSeq("float") -> 1.234F,
    PathSeq("double") -> 12345.6789D,
    PathSeq("bool") -> true,
    PathSeq("null") -> null,
    PathSeq("string") -> "a simple string"
  ).map(p => (p._1,UConfigValue(p._2))))

  val complexObj = createEUT(Seq(
    PathSeq("obj","int") -> 42,
    PathSeq("obj","sub","double") -> 1234.56789,
    PathSeq("obj","bool") -> true,
    PathSeq("obj","sub","bool") -> false,
    PathSeq("string") -> "a simple string",
    PathSeq("other","obj","a") -> "a",
    PathSeq("other","obj","b") -> "b"
  ).map(p => (p._1,UConfigValue(p._2))))

  val tests = Tests {
    'withFallback_find-{
      val foo_a = UConfigObject.empty.withPathValue( PathSeq("foo","a"), UConfigValue(42) )
      val foo_b = UConfigObject.empty.withPathValue( PathSeq("foo","b"), UConfigValue(43) )
      val foo_c = UConfigObject.empty.withPathValue( PathSeq("foo"),
        UConfigObject.empty
          .withPathValue( PathSeq("c"), UConfigValue(44) )
          .withPathValue( PathSeq("d"), UConfigValue(-1) )
      )
      val foo_d = UConfigObject.empty.withPathValue( PathSeq("foo","d"), UConfigValue(45) )

      assert( UConfigValue(42).withFallback(UConfigValue(-1)).asInt == 42 )
      assert( UConfigValue(1).withFallback(foo_a).asInt == 1 )

      val foo_a_b_c_d: UConfigObject = foo_d.withFallback(foo_c).withFallback(foo_b).withFallback(foo_a).asObject
      assert(
        foo_a_b_c_d.find("foo.a").get.asInt == 42,
        foo_a_b_c_d.find("foo.b").get.asInt == 43,
        foo_a_b_c_d.find("foo.c").get.asInt == 44,
        foo_a_b_c_d.find("foo.d").get.asInt == 45
      )
    }

    'withPathValue_find-{
      val obj = UConfigObject.empty
        .withPathValue(PathSeq("int"),UConfigValue(1))
        .withPathValue(PathSeq("obj","int"),UConfigValue(42))
        .withPathValue(PathSeq("obj","double"), UConfigValue(-1.0))
        .withPathValue(PathSeq("obj"),
          UConfigObject.empty
            .withPathValue(PathSeq("bool"),UConfigValue(true))
            .withPathValue(PathSeq("double"),UConfigValue(43.0))
            .withPathValue(PathSeq("sub"),UConfigValue(1))
        )
        .withPathValue(PathSeq("obj","sub"),
          UConfigObject.empty
            .withPathValue(PathSeq("string"),UConfigValue("string"))
            .withPathValue(PathSeq("int"), UConfigObject.empty )
        )
        .withPathValue(PathSeq("obj","sub","int"),UConfigValue(1234))

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
          simpleObj.values().asScala.toSet ==> Seq(42,123456789L,1.234F,12345.6789D,true,null,"a simple string").map(UConfigValue.apply).toSet
        }
        'get-{
          simpleObj.get("int") ==> UConfigValue(42)
          simpleObj.get("long") ==> UConfigValue(123456789L)
          simpleObj.get("float") ==> UConfigValue(1.234F)
          simpleObj.get("double") ==> UConfigValue(12345.6789D)
          simpleObj.get("bool") ==> UConfigValue(true)
          simpleObj.get("null") ==> UConfigValue(null)
          simpleObj.get("string") ==> UConfigValue("a simple string")
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


