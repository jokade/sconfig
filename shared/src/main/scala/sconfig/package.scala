import com.typesafe.config.{Key, Path}
//     Project: sconfig
//      Module: 
// Description: 

package object sconfig {

  type PathSeq = Seq[Path]
  object PathSeq {
    def apply(segments: Key*): PathSeq = Seq(segments:_*)

    implicit class RichPathSeq(val ps: PathSeq) extends AnyVal {
      def toPath: String = ps.mkString(".")
    }
  }
}
