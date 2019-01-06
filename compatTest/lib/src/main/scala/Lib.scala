import com.typesafe.config._

object Lib {
  lazy val config = ConfigFactory.load()

  def int: Int = config.getInt("foo.int")
}
