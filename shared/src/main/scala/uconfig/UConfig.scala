//     Project: sconfig
//      Module: shared
// Description: sconfig implementation of Config
package uconfig

import java.{lang, util}
import java.util.Map

import com.typesafe.config._
import uconfig.parser.HoconParser

case class UConfig(root: UConfigObject) extends Config {
//  def root(): SConfigObject = _root

  @inline final override def atKey(key: Key): Config = ???
  @inline final override def atPath(path: Path): Config = ???
  @inline final override def chckValid(reference: Config, restrictToPaths: Path*): Unit = ???
  @inline final override def entrySet(): util.Set[Map.Entry[String, ConfigValue]] = ???
  @inline final override def getAnyRef(path: Path): AnyRef = ???
  @inline final override def getAnyRefList[T <: Object](path: Path): util.List[T] = ???
  @inline final override def getBoolean(path: Path): Boolean = findOrThrow(path).asBoolean
  @inline final override def getBooleanList(path: Path): util.List[lang.Boolean] = findOrThrow(path).asBooleanList
  @inline final override def getInt(path: Path): java.lang.Integer = findOrThrow(path).asInt
  @inline final override def getIntList(path: Path): util.List[Integer] = findOrThrow(path).asIntList
  @inline final override def getLong(path: Path): java.lang.Long = findOrThrow(path).asLong
  @inline final override def getLongList(path: Path): util.List[lang.Long] = findOrThrow(path).asLongList
  @inline final override def getString(path: Path): String = findOrThrow(path).asString
  @inline final override def getStringList(path: Path): util.List[Path] = findOrThrow(path).asStringList
  @inline final override def getBytes(path: String): lang.Long = ???
  @inline final override def getBytesList(path: String): util.List[lang.Long] = ???
  @inline final override def getConfig(path: Path): Config = ???
  @inline final override def getConfigList(path: Path): util.List[Config] = ???
  @inline final override def getDouble(path: Path): lang.Double = findOrThrow(path).asDouble
  @inline final override def getDoubleList(path: Path): util.List[lang.Double] = findOrThrow(path).asDoubleList
  override def withFallback(other: ConfigMergeable): UConfig = root.withFallback(other) match {
    case obj: UConfigObject => UConfig(obj)
  }

  def findOrThrow(path: Path): UConfigValue = find(path).getOrElse(throw new ConfigException.Missing(path))

  def find(path: Path): Option[UConfigValue] = root.find(path)

}

object UConfig {
  def apply(config: String): UConfig = HoconParser.parseAll(HoconParser.root,config) match {
    case HoconParser.Success(root,_) => UConfig(root)
    case HoconParser.Failure(msg,_) => throw new ConfigException(ConfigOrigin.StringOrigin(config),msg)
    case HoconParser.Error(msg,_) => throw new ConfigException(ConfigOrigin.StringOrigin(config),msg)
  }

  val empty = UConfig(UConfigObject.empty)
}
