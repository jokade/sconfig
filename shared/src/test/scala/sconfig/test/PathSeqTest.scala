//     Project: sconfig
//      Module: shared / test
// Description: Tests for PathSeq
package sconfig.test

import sconfig.PathSeq
import PathSeq._
import utest._

object PathSeqTest extends TestSuite {
  val tests = TestSuite {
    val complexPath = "\"a complex {} [.+*#$]:path=\".with-multiple.\" quoted \".and-unquoted.\"seg!ments\""
    val complexPathSeq = PathSeq("\"a complex {} [.+*#$]:path=\"","with-multiple","\" quoted \"","and-unquoted","\"seg!ments\"")

    'toPath-{
      assert( complexPathSeq.toPath == complexPath )
    }
    'fromString-{
      assert( PathSeq.fromString(complexPath) == complexPathSeq )
    }
  }
}
