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

  val tests = TestSuite {
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
    }
  }

  implicit def valueToObject(value: ConfigValue): ConfigObject = value match {
    case o: ConfigObject => o
  }
}


