//     Project: sconfig
//      Module: shared / test
// Description: Tests for SConfigObject.MapConfigObject
package sconfig.test

import sconfig.SConfigObject.MapConfigObject
import sconfig.{PathSeq, SConfigObject, SConfigValue}
import utest._

object MapConfigObjectTest extends SConfigObjectTests {
  override def createEUT(pairs: Seq[Tuple2[PathSeq, SConfigValue]]) = SConfigObject(pairs)
}
