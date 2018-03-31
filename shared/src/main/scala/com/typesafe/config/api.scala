//     Project: sconfig
//      Module:
// Description:
package com.typesafe.config

trait Config extends ConfigMergeable {
  def root(): ConfigObject
  def atKey(key: Key): Config
  def atPath(path: Path): Config
  def chckValid(reference: Config, restrictToPaths: Path*): Unit
  def entrySet(): java.util.Set[java.util.Map.Entry[String,ConfigValue]]
  def getAnyRef(path: Path): Object
  def getAnyRefList[T<:Object](path: Path): java.util.List[T]
  def getBoolean(path: Path): Boolean
  def getBooleanList(path: Path): java.util.List[java.lang.Boolean]
  def getInt(path: Path): java.lang.Integer
  def getIntList(path: Path): java.util.List[java.lang.Integer]
  def getLong(path: Path): java.lang.Long
  def getLongList(path: Path): java.util.List[java.lang.Long]
  def getBytes(path: String): java.lang.Long
  def getBytesList(path: String): java.util.List[java.lang.Long]
  def getConfig(path: Path): Config
  def getConfigList(path: Path): java.util.List[Config]
  def getDouble(path: Path): java.lang.Double
  def getDoubleList(path: Path): java.util.List[java.lang.Double]
  def getString(path: Path): String
  def getStringList(path: Path): java.util.List[String]
//  def getDuration(path: )
}

trait ConfigOrigin {
  def description(): String
  def filename(): String
  def resource(): String
  def lineNumber(): Int
}

object ConfigOrigin {
  case class StringOrigin(description: String, lineNumber: Int = -1) extends ConfigOrigin {
    override def filename(): String = null
    override def resource(): String = null
  }
}


trait ConfigMergeable {
  def withFallback(other: ConfigMergeable): ConfigMergeable
}

trait ConfigValue extends ConfigMergeable {
  def atKey(key: Key): Config
  def atPath(path: Path): Config
  def origin(): ConfigOrigin
  def render(): HoconString
  def render(options: ConfigRenderOptions): HoconString
  def unwrapped(): Object
  def valueType(): ConfigValueType
  override def withFallback(other: ConfigMergeable): ConfigValue
  def withOrigin(origin: ConfigOrigin): ConfigValue
}

trait ConfigObject extends ConfigValue with java.util.Map[String,ConfigValue] {
  def withValue(key: Key, value: ConfigValue): ConfigValue
}


trait ConfigRenderOptions

sealed trait ConfigValueType
object ConfigValueType {
  object BOOLEAN extends ConfigValueType { override def toString = "BOOLEAN" }
  object LIST extends ConfigValueType { override def toString = "LIST" }
  object NULL extends ConfigValueType { override def toString = "NULL" }
  object NUMBER extends ConfigValueType { override def toString = "NUMBER" }
  object OBJECT extends ConfigValueType { override def toString = "OBJECT" }
  object STRING extends ConfigValueType { override def toString = "STRING" }
}