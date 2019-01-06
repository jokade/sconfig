//     Project: sconfig
//      Module: shared / test
// Description: Tests for SConfigObject.MapConfigObject
package uconfig.test

import uconfig.UConfigObject.MapConfigObject
import uconfig.{PathSeq, UConfigObject, UConfigValue}
import utest._

object MapConfigObjectTest extends SConfigObjectTests {
  override def createEUT(pairs: Seq[Tuple2[PathSeq, UConfigValue]]) = UConfigObject(pairs)
}
