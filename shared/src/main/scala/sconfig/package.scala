import com.typesafe.config.{ConfigException, Key, Path}
import fastparse.core.Parsed
import sconfig.parser.HoconParser
//     Project: sconfig
//      Module: 
// Description: 

package object sconfig {

  type PathSeq = List[Path]
  object PathSeq {
    def apply(segments: Key*): PathSeq = List(segments:_*)

    def fromString(path: String): PathSeq = HoconParser.pathSeq.parse(path) match {
      case Parsed.Success(seq,_) => seq
      case Parsed.Failure(_,_,extra) => throw new ConfigException.BadPath(path,extra.toString)
    }

    implicit class RichPathSeq(val ps: PathSeq) extends AnyVal {
      def toPath: String = ps.mkString(".")
    }
  }
}
