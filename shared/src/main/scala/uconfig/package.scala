import com.typesafe.config.{ConfigException, Key, Path}
import uconfig.parser.HoconParser

package object uconfig {

  type PathSeq = List[Path]
  object PathSeq {
    def apply(segments: Key*): PathSeq = List(segments:_*)

    def fromString(path: String): PathSeq = HoconParser.parse(HoconParser.pathSeq,path) match {
      case HoconParser.Success(seq,_) => seq
      case HoconParser.Failure(msg,_) => throw new ConfigException.BadPath(path,msg)
      case HoconParser.Error(msg,_) => throw new ConfigException.BadPath(path,msg)
    }

    implicit class RichPathSeq(val ps: PathSeq) extends AnyVal {
      def toPath: String = ps.mkString(".")
    }
  }
}
