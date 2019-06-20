package uconfig.parser.test

import scala.language.implicitConversions
import uconfig.{PathSeq, UConfigValue}
import uconfig.UConfigValue.{AtomicValue, DoubleValue, FalseValue, ListValue, LongValue, StringValue, TrueValue}
import uconfig.parser.HoconParser
import HoconParser.Pair
import utest._

object HoconParserTest extends TestSuite {
  val tests = Tests {
    'unquotedString-{
      implicit val eut = HoconParser.unquotedString

      testUnquotedString("a.path","a.path")
      testUnquotedString("  a.path.with.padding\t ","a.path.with.padding")
      testUnquotedString(" an unquoted \t string with   whitespace\t\tin between \t","an unquoted \t string with   whitespace\t\tin between")
      testUnquotedString(" simple 'quotes are not considered' as quotes' ","simple 'quotes are not considered' as quotes'")

      testUnquotedString(" an_unquoted-string until $ ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until \" ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until { ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until } ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until [ ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until ] ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until : ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until = ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until , ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until + ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until # ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until ` ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until ^ ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until ? ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until ! ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until @ ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until * ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until & ignored","an_unquoted-string until")
      testUnquotedString(" an_unquoted-string until \\ ignored","an_unquoted-string until")

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
      implicit val eut = HoconParser.numberValue

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
      implicit val eut = HoconParser.boolValue

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

    'list-{
      implicit val eut = HoconParser.listValue

      test("[]",Seq[Int]())
      test("[1,2,3]",Seq(1,2,3))
      test("[  1 ,2, 3  ]",Seq(1,2,3))
      test("[1]",Seq(1))
      test("[1.0, 2.0, 3.0, 4.0]",Seq(1.0,2.0,3.0,4.0))
      test("[true, false]",ListValue(Seq(TrueValue,FalseValue)))
      test("[ hello , world ]",ListValue(Seq(StringValue("hello",false),StringValue("world",false))))
      test("[\" hello \" , \" world \"]",Seq(" hello "," world "))
    }

    'pair-{
      implicit val eut = HoconParser.pair

      test("key:value",(PathSeq("key"),"value"))
      test(" key\t :\t \t value",(PathSeq("key"),"value"))
      test("key=value",(PathSeq("key"),"value"))
      test("\tkey\t=\tvalue",(PathSeq("key"),"value"))
      test("  \t  key  \t=   value  \n  ",(PathSeq("key"),"value"))
      test("  a.simple.path\t =\twith a value  \t  ", (PathSeq("a","simple","path"),"with a value"))
      test("  a.simple.path\t =\twith a value  \t  ", (PathSeq("a","simple","path"),"with a value"))
    }

    'pathSeq-{
      implicit val eut = HoconParser.pathSeq

      test("a.simple.path",PathSeq("a","simple","path"))
      test(" 10.0a.b ",PathSeq("10","0a","b"))
      test("\t \t _.-.%    ",PathSeq("_","-","%"))
      test("a.path with unquoted.whitespace",PathSeq("a","path"))
//      test("   \" a quoted string \"  .x",PathSeq("\" a quoted string \""))
      test("a.path.\"with.a quoted\".segment",PathSeq("a","path","\"with.a quoted\"","segment"))

      val complexPath = "\"a complex {} [.+*#$]:path=\".with-multiple.\" quoted \".and-unquoted.\"seg!ments\"   "
      test(complexPath, PathSeq("\"a complex {} [.+*#$]:path=\"","with-multiple","\" quoted \"","and-unquoted","\"seg!ments\""))
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
          |
          |  stringList = ["hello ", world  ]
          |  emptyList = [ ]
          |  intList = [ 1,2,  3  ]
          |}
        """.stripMargin

      val HoconParser.Success(res,_) = HoconParser.parse(HoconParser.obj,objString)

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
      res.strings("stringList") ==> Seq("hello ","world")
      res.strings("emptyList").size ==> 0
      res.ints("intList") ==> Seq(1,2,3)
    }
  }

  def qw(s: String): UConfigValue = StringValue(s)
  implicit def pair(p: (PathSeq,String)): (PathSeq,UConfigValue) = (p._1,StringValue(p._2,false))
  implicit def stringValue(s: String): AtomicValue = StringValue(s,false)
  implicit def intValue(i: Int): AtomicValue = LongValue(i)
  implicit def doubleValue(d: Double): AtomicValue = DoubleValue(d)
  implicit def boolValue(b: Boolean): AtomicValue = if(b) TrueValue else FalseValue
  implicit def intListValue(seq: Seq[Int]): ListValue = ListValue(seq.map(LongValue(_)))
  implicit def stringListValue(seq: Seq[String]): ListValue = ListValue(seq.map(StringValue(_)))
  implicit def doubleListValue(seq: Seq[Double]): ListValue = ListValue(seq.map(DoubleValue(_)))

  def testUnquotedString(input: String, expectedResult: String) = HoconParser.parse(HoconParser.unquotedString,input) match {
    case HoconParser.Success(result, _) => assert(result == expectedResult)
    case HoconParser.Failure(msg,_) => throw new RuntimeException(msg)
    case HoconParser.Error(msg,_) => throw new RuntimeException(msg)
  }

  def test(input: String, expectedResult: UConfigValue)(implicit parser: HoconParser.Parser[UConfigValue]) = HoconParser.parse(parser,input) match {
    case HoconParser.Success(res,_) => assert( res == expectedResult )
    case HoconParser.Failure(msg,_) => throw new RuntimeException(msg)
    case HoconParser.Error(msg,_) => throw new RuntimeException(msg)
  }

  def test(input: String, expectedResult: Pair)(implicit parser: HoconParser.Parser[Pair]) = HoconParser.parse(parser,input) match {
    case HoconParser.Success(res,_) => assert( res == expectedResult )
    case HoconParser.Failure(msg,_) => throw new RuntimeException(msg)
    case HoconParser.Error(msg,_) => throw new RuntimeException(msg)
  }

  def test(input: String, expectedResult: PathSeq)(implicit parser: HoconParser.Parser[PathSeq]) = HoconParser.parse(parser,input) match {
    case HoconParser.Success(res,_) => assert( res == expectedResult )
    case HoconParser.Failure(msg,_) => throw new RuntimeException(msg)
    case HoconParser.Error(msg,_) => throw new RuntimeException(msg)
  }

  def testPairs(input: String, expected: Pair*)(implicit parser: HoconParser.Parser[Seq[Pair]]): Unit = HoconParser.parse(parser,input) match {
    case HoconParser.Success(res,_) => assert( res == expected )
    case HoconParser.Failure(msg,_) => throw new RuntimeException(msg)
    case HoconParser.Error(msg,_) => throw new RuntimeException(msg)
  }

  def fails(input: String)(implicit parser: HoconParser.Parser[_]) = HoconParser.parse(parser,input) match {
    case HoconParser.Failure(_,_) =>
    case HoconParser.Error(_,_) | HoconParser.Success(_,_) => throw new RuntimeException(s"expected Failure for input: '$input'")
  }
}
