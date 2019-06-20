package uconfig.parser

import uconfig.{PathSeq, UConfigObject, UConfigValue}
import uconfig.UConfigValue.{AtomicValue, BooleanValue, FalseValue, ListValue, NumberValue, StringValue, TrueValue}

import scala.util.parsing.combinator.JavaTokenParsers

object HoconParser extends JavaTokenParsers {

  type Pair = (PathSeq,UConfigValue)

  val unquotedString: Parser[String] = """[^$"{}\[\]:=,+#`\^?!@*&\\]+""".r ^^ (_.trim)
  val unquotedStringValue: Parser[StringValue] = unquotedString ^^ (StringValue(_,quoted = false))

  val unquotedPathSegment: Parser[String] = """[^\s\.$"{}\[\]:=,+#`\^?!@*&\\]+""".r ^^ (_.trim)
  val quotedPathSegment: Parser[String] = stringLiteral

  // TODO: don't use stripPrefix/Suffix
  val stringValue: Parser[StringValue] = stringLiteral ^^ ( s => StringValue(s.stripPrefix("\"").stripSuffix("\"")))

  val numberValue: Parser[NumberValue] = floatingPointNumber ^^ (NumberValue(_)) | wholeNumber ^^ (NumberValue(_))

  val boolValue: Parser[BooleanValue] = "true" ^^ (_ => TrueValue) | "false" ^^(_ => FalseValue)

  val atomicValue: Parser[AtomicValue] = boolValue | numberValue | stringValue | unquotedStringValue

  val listValue: Parser[ListValue] = "[" ~> repsep(atomicValue,",") <~ "]" ^^ (ListValue(_))

  val value: Parser[UConfigValue] = atomicValue | listValue

  val pathSeq: Parser[PathSeq] = rep1sep(unquotedPathSegment | quotedPathSegment, ".")
  val pair: Parser[Pair] = ( pathSeq ~ obj ^^ (p => (p._1,p._2) ) | ( pathSeq ~ (":" | "=") ~ value) ^^( p => (p._1._1,p._2) ) )

  val comment: Parser[Unit] = ("//" | "#") ~ """[^\n]*""".r  ^^( _ => ())
  val spaceOrCommentOrNewline: Parser[Unit] = (whiteSpace | comment | "\n" ) ^^ (_ => ())

  val pairs: Parser[Seq[Pair]] = opt(spaceOrCommentOrNewline) ~> rep( opt(spaceOrCommentOrNewline) ~> pair <~ opt(spaceOrCommentOrNewline))

  val obj: Parser[UConfigObject] = "{" ~> pairs <~ "}" ^^(UConfigObject(_))

  def root: Parser[UConfigObject] = rep(spaceOrCommentOrNewline) ~> pairs <~ rep(spaceOrCommentOrNewline) ^^ UConfigObject.apply
}
