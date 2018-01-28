//     Project: sconfig
//      Module:
// Description:
package sconfig.parser.test

import scala.language.implicitConversions
import fastparse.all.{Parsed, Parser}
import Parsed.{Failure, Success}
import sconfig.parser.HoconParser
import sconfig.{PathSeq, SConfigValue}
import PathSeq._
import sconfig.SConfigValue.{apply => _, _}
import sconfig.parser.HoconParser.Pair
import utest._

object HoconParserTest extends TestSuite {

  def qw(s: String): SConfigValue = StringValue(s)
  implicit def pair(p: (PathSeq,String)): (PathSeq,SConfigValue) = (p._1,StringValue(p._2,false))
  implicit def stringValue(s: String): SConfigValue = StringValue(s,false)
  implicit def boolValue(b: Boolean): SConfigValue = if(b) TrueValue else FalseValue
  implicit def longValue(l: Long): SConfigValue = LongValue(l)
  implicit def doubleValue(d: Double): SConfigValue = DoubleValue(d)

  val tests = TestSuite {

    'space-{
      import HoconParser.space.parse

      val Failure(_,_,_) = parse("")
      val Failure(_,_,_) = parse("\n")
      val Failure(_,_,_) = parse("a \t b")
      val Success(_,5) = parse("  \t  \n ")
    }

    'unquotedStringValue-{
      implicit val eut = HoconParser.unquotedStringValue

      test("a.path","a.path")
      test("  a.path.with.padding\t ","a.path.with.padding")
      test(" an unquoted \t string with   whitespace\t\tin between \t","an unquoted \t string with   whitespace\t\tin between")
      test(" simple 'quotes are not considered' as quotes' ","simple 'quotes are not considered' as quotes'")

      test(" an_unquoted-string until $ ignored","an_unquoted-string until")
      test(" an_unquoted-string until \" ignored","an_unquoted-string until")
      test(" an_unquoted-string until { ignored","an_unquoted-string until")
      test(" an_unquoted-string until } ignored","an_unquoted-string until")
      test(" an_unquoted-string until [ ignored","an_unquoted-string until")
      test(" an_unquoted-string until ] ignored","an_unquoted-string until")
      test(" an_unquoted-string until : ignored","an_unquoted-string until")
      test(" an_unquoted-string until = ignored","an_unquoted-string until")
      test(" an_unquoted-string until , ignored","an_unquoted-string until")
      test(" an_unquoted-string until + ignored","an_unquoted-string until")
      test(" an_unquoted-string until # ignored","an_unquoted-string until")
      test(" an_unquoted-string until ` ignored","an_unquoted-string until")
      test(" an_unquoted-string until ^ ignored","an_unquoted-string until")
      test(" an_unquoted-string until ? ignored","an_unquoted-string until")
      test(" an_unquoted-string until ! ignored","an_unquoted-string until")
      test(" an_unquoted-string until @ ignored","an_unquoted-string until")
      test(" an_unquoted-string until * ignored","an_unquoted-string until")
      test(" an_unquoted-string until & ignored","an_unquoted-string until")
      test(" an_unquoted-string until \\ ignored","an_unquoted-string until")

      fails("")
    }

    'stringValue-{
      implicit val eut = HoconParser.stringValue

      test("\"a simple string\"", qw("a simple string"))
      test("\"escaped quote at the end\\\"\\\"\"",qw("escaped quote at the end\\\"\\\""))
      test("\"string with escaped \\\" quote\"",qw("string with escaped \\\" quote"))
      test("\"string with 2 consecutive escaped \\\"\\\" quotes\"",qw("string with 2 consecutive escaped \\\"\\\" quotes"))
      test("\"string with \\\"embedded\\\" string\"",qw("string with \\\"embedded\\\" string"))
    }

    'number-{
      implicit val eut = HoconParser.number

      test("0",0)
      test("123456789",123456789)
      test("-123456789",-123456789)
      test("00123",123)
      test("123456789.1234567891234567",123456789.1234567891234567)
      test("-0123456789.1234567891234567",-123456789.1234567891234567)
      test("1e3",1e3)
      test("0e+1234567",0.0)
      test("1234.56789E42",1234.56789E42)
      test("-1234.56789E-42",-1234.56789E-42)

      fails("")
      test("42a3",42)
      fails("a1")
      fails("\"42\"")
    }

    'bool-{
      implicit val eut = HoconParser.bool

      test("true",true)
      test("false",false)
      fails("tru")
      fails("1")
      fails("0")
      fails("t")
      fails("f")
      fails("T")
      fails("F")
      fails("TRUE")
      fails("FALSE")
    }

    'pair-{
      implicit val eut = HoconParser.pair

      test("key:value",(PathSeq("key"),"value"))
      test(" key\t :\t \t value",(PathSeq("key"),"value"))
      test("key=value",(PathSeq("key"),"value"))
      test("\tkey\t=\tvalue",(PathSeq("key"),"value"))
      test("  \t  key  \t=   value  \n  ",(PathSeq("key"),"value"))
      test("a.simple.path=with a value", (PathSeq("a","simple","path"),"with a value"))
      test("  a.simple.path\t =\twith a value  \t  ", (PathSeq("a","simple","path"),"with a value"))
//      test(" key")
//      test(key = value""",("key","value"))
    }

    'pathSeq-{
      implicit val eut = HoconParser.pathSeq

      test("a.simple.path",PathSeq("a","simple","path"))
      test(" 10.0a.b ",PathSeq("10","0a","b"))
      test("\t \t _.-.%    ",PathSeq("_","-","%"))
      test("a.path with unquoted.whitespace",PathSeq("a","path"))
      test("   \" a quoted string \"  .x",PathSeq("\" a quoted string \""))
      test("a.path.\"with.a quoted\".segment",PathSeq("a","path","\"with.a quoted\"","segment"))

      val complexPath = "\"a complex {} [.+*#$]:path=\".with-multiple.\" quoted \".and-unquoted.\"seg!ments\"   "
      test(complexPath, PathSeq("\"a complex {} [.+*#$]:path=\"","with-multiple","\" quoted \"","and-unquoted","\"seg!ments\""))

      val Parsed.Success(res,_) =  eut.parse(complexPath)
      assert( res.toPath == complexPath.trim )
//      test("  a . path \twith . whitespace\t",Seq("a "," path \twith "," whitespace"))
    }

    'simplePairs-{
      implicit val eut = HoconParser.pairs

      testPairs("")
      testPairs("  # a comment\n   \t ")
      testPairs("key = value",PathSeq("key") -> "value")
      testPairs("key = value # comment",PathSeq("key") -> "value")
      testPairs("key1 = value1 # comment\n key2 = value2",PathSeq("key1") -> "value1",PathSeq("key2")->"value2")
      testPairs(
        """// comment
          |key.int=42
          |
          | # other comment
          |    key.bool : true // comment at end
          |a."path with ".spaces    =    1234.5678
        """.stripMargin,
        PathSeq("key","int") -> 42,
        PathSeq("key","bool") -> true,
        PathSeq("a","\"path with \"","spaces") -> 1234.5678
      )
    }

    'obj-{
      val objString =
        """{
          |  int = 42
          |long:123456789
          |  float: -12.34
          |  # float: 4.2
          |  // a comment
          |  double = 1.23 # comment
          |  string:"string with spaces"
          |  bool   =true
          |
          |  sub.y = 1
          |  sub {
          |    x.y = z
          |
          |    "a key" = 42
          |
          |    x.a = false
          |
          |    x.a {
          |     // comment
          |     b = false
          |    }
          |
          |    x.a.c {
          |     d = 1
          |    }
          |    x.a.c = "another string"
          |  }
          |}
        """.stripMargin

      val Parsed.Success(res,_) = HoconParser.obj.parse(objString)

      res.int("int")       ==> 42
      res.long("long")     ==> 123456789L
      res.float("float")   ==> -12.34F
      res.double("double") ==> 1.23
      res.string("string") ==> "string with spaces"
      res.boolean("bool")  ==> true
      val sub = res.obj("sub")
      sub.int("y") ==> 1
//      sub.int("\"a key\"")     ==> 42
      val x = sub.obj("x")
      x.string("y") ==> "z"
      x.obj("a").boolean("b") ==> false
      x.obj("a").string("c")  ==> "another string"
    }
  }


  def test(input: String, expected: SConfigValue)(implicit parser: Parser[SConfigValue]): Unit = parser.parse(input) match {
    case Parsed.Success(res,_) => assert(res == expected)
    case Parsed.Failure(expected, failIndex, extra) => throw new RuntimeException(s"Failed at index $failIndex of input: '$input'; expected: $expected")
  }

  def test(input: String, expected: Pair)(implicit parser: Parser[Pair]): Unit = parser.parse(input) match {
    case Parsed.Success(res,_) => assert(res._1==expected._1, res._2==expected._2)
    case Parsed.Failure(expected, failIndex, extra) => throw new RuntimeException(s"Failed at index $failIndex of input: '$input'; expected: $expected")
  }

  def test(input: String, expected: PathSeq)(implicit parser: Parser[PathSeq]): Unit = parser.parse(input) match {
    case Parsed.Success(res,_) => assert(res == expected)
    case Parsed.Failure(expected, failIndex, extra) => throw new RuntimeException(s"Failed at index $failIndex of input: '$input'; expected: $expected")
  }

  def testPairs(input: String, expected: Pair*)(implicit parser: Parser[Seq[Pair]]): Unit = parser.parse(input) match {
    case Parsed.Success(res,_) => assert(res == expected)
    case Parsed.Failure(expected, failIndex, extra) => throw new RuntimeException(s"Failed at index $failIndex of input: '$input'; expected: $expected")
  }

  def fails(input: String)(implicit parser: Parser[_]): Unit =
    parser.parse(input) match {
      case Parsed.Failure(expected, failIndex, extra) =>
      case Parsed.Success(_,_) => throw new RuntimeException(s"expected Failure for input:'$input'")
    }
}
