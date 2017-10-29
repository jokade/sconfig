//     Project: sconfig
//      Module: shared
// Description: Fastparse parser for HOCON files.
package sconfig.parser

import fastparse.all._
import sconfig.{PathSeq, SConfigObject, SConfigValue}

/**
 * Parser for [[https://github.com/unicredit/shocon/blob/master/shared/src/main/scala/eu/unicredit/shocon/ConfigParser.scala HOCON]] config files.
 */
object HoconParser {
  import sconfig.SConfigValue._

  type Pair = (PathSeq,SConfigValue)

  def isWhitespace(c: Char): Boolean = c match {
    case ' '|'\n'|'\u00A0'|'\u2007'|'\u202F'|'\uFEFF' /* BOM */ => true;
    case _ => Character.isWhitespace(c);
  }

  def isWhitespaceNoNewline(c: Char): Boolean = c != '\n' && isWhitespace(c)

//  val letter   = P( CharIn('a' to 'z', 'A' to 'Z') )
  val digit: Parser[Unit] = P( CharIn('0' to '9') )
//  val hexdigit = P( CharIn('0'to'9', 'a' to 'f', 'A' to 'F') )

  // whitespace
  val comment: Parser[Unit] = P( ("//" | "#") ~ CharsWhile(_ != '\n') )
//  val nlspace = P( (CharsWhile(isWhitespace _, min = 1) | comment ).rep )
//  val space   = P( ( CharsWhile(isWhitespaceNoNewline _, min = 1) | comment ).rep )
  val space: Parser[Unit] = P ( CharsWhile(isWhitespaceNoNewline _ ) )
  val spaceOrNewline: Parser[Unit] = P( CharsWhile(isWhitespace _) )
  val spaceOrCommentOrNewline: Parser[Unit] = P( CharsWhile(isWhitespace _) | comment )

  private def isUnquotedStringChar(c: Char) = c match {
    case '$' | '"' | '{' | '}' | '[' | ']' | ':' | '=' | ',' | '+' | '#' | '`' | '^' | '?' | '!' | '@' | '*' | '&' | '\\' => false
    case _ => true
  }
  val unquotedString: Parser[String] = P( CharsWhile(isUnquotedStringChar _).!.map(_.trim) )
  val unquotedStringValue: Parser[StringValue] = unquotedString.map(StringValue(_,quoted=false))

  private def isUnquotedPathSegmentChar(c: Char) = c != '.' && !isWhitespace(c) && isUnquotedStringChar(c)
  val unquotedPathSegment: Parser[String] = P( CharsWhile(isUnquotedPathSegmentChar _) ).!
  val quotedPathSegment: Parser[String] = P( string ).map("\""+_+"\"")

  val string: Parser[String]  = P("\"" ~ ("\\\"" | (!"\"" ~ AnyChar)).rep.! ~ "\"")
  val stringValue: Parser[StringValue] = string.map(StringValue(_))

  val bool:   Parser[BooleanValue] = P( "true" | "false" ).!.map{
    case "true" => TrueValue
    case _ => FalseValue
  }

  val number: Parser[NumberValue] = P( "-".? ~ digit.rep(1) ~ ("." ~ digit.rep).? ~ (("e"|"E") ~ ("+"|"-").? ~ digit.rep(1)).? ).!
    .map(NumberValue(_))

  val value: Parser[SConfigValue] = P( bool | number | stringValue | unquotedStringValue | "null".!.map(_ => NullValue) )
  val pathSeq: Parser[PathSeq]    = P ( space.rep ~ ((quotedPathSegment | unquotedPathSegment) ~ ".").rep ~ (quotedPathSegment | unquotedPathSegment) ~ space.rep ).map( p => PathSeq((p._1 :+ p._2):_*) )
  val pair: Parser[Pair]          = P( space.? ~ pathSeq ~ space.? ~ ( obj | ((":"|"=") ~/ space.? ~ value ) ) )

  val obj:   Parser[SConfigObject] = P( "{" ~ pairs ~ "}" ).map(SConfigObject.apply)
  val pairs: Parser[Seq[Pair]]    = P( (spaceOrCommentOrNewline.rep ~ pair ~ spaceOrCommentOrNewline.rep).rep )

  val root: Parser[SConfigObject] = P( spaceOrCommentOrNewline.rep ~ "{".? ~ pairs ~ "}".? ~ spaceOrCommentOrNewline.rep ).map(SConfigObject.apply)

}
