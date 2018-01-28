//     Project: sconfig
//      Module: shared
// Description: sconfig implementation of Config
package sconfig

import java.{lang, util}
import java.util.Map

import com.typesafe.config._
import fastparse.core.Parsed
import sconfig.parser.HoconParser

case class SConfig(root: SConfigObject) extends Config {
//  def root(): SConfigObject = _root

  override def atKey(key: Key): Config = ???
  override def atPath(path: Path): Config = ???
  override def chckValid(reference: Config, restrictToPaths: Path*): Unit = ???
  override def entrySet(): util.Set[Map.Entry[String, ConfigValue]] = ???
  override def getAnyRef(path: Path): AnyRef = ???
  override def getAnyRefList[T <: Object](path: Path): util.List[T] = ???
  override def getBoolean(path: Path): Boolean = findOrThrow(path).asBoolean
  override def getInt(path: Path): Int = findOrThrow(path).asInt
  override def getLong(path: Path): Long = findOrThrow(path).asLong
  override def getString(path: Path): String = findOrThrow(path).asString
  override def getBooleanList(path: Path): util.List[lang.Boolean] = ???
  override def getBytes(path: String): lang.Long = ???
  override def getBytesList(path: String): util.List[lang.Long] = ???
  override def getConfig(path: Path): Config = ???
  override def getConfigList(path: Path): util.List[Config] = ???
  override def getDouble(path: Path): lang.Double = findOrThrow(path).asDouble
  override def getDoubleList(path: Path): util.List[lang.Double] = ???
  override def withFallback(other: ConfigMergeable): ConfigMergeable = ???

  def findOrThrow(path: Path): SConfigValue = find(path).getOrElse(throw new ConfigException.Missing(path))

  def find(path: Path): Option[SConfigValue] = root.find(path)

}

object SConfig {
  def apply(config: String): SConfig = HoconParser.root.parse(config) match {
    case Parsed.Success(root,_) => SConfig(root)
    case Parsed.Failure(_,_,extra) => throw new ConfigException(ConfigOrigin.StringOrigin(config),extra.toString)
  }
}
