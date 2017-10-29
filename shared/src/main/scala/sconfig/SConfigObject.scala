//     Project: sconfig
//      Module: shared
// Description: sconfig implementation of ConfigObject
package sconfig

import java.util

import com.typesafe.config._

import scala.annotation.tailrec
import scala.collection.MapLike

trait SConfigObject extends SConfigValue with ConfigObject {
  override def clear(): Unit = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def put(key: String, value: ConfigValue): ConfigValue = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def putAll(m: util.Map[_ <: String, _ <: ConfigValue]): Unit = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def remove(key: scala.Any): ConfigValue = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def valueType(): ConfigValueType = ConfigValueType.OBJECT

  override def asObject: SConfigObject = this
  def apply(key: String): SConfigValue
  def int(key: String): Int = apply(key).asInt
  def long(key: String): Long = apply(key).asLong
  def float(key: String): Float = apply(key).asFloat
  def double(key: String): Double = apply(key).asDouble
  def boolean(key: String): Boolean = apply(key).asBoolean
  def string(key: String): String = apply(key).asString
  def obj(key: String): SConfigObject = apply(key).asObject

  def withPathValue(path: PathSeq, value: SConfigValue): SConfigObject
}

object SConfigObject {

  case class MapConfigObject(map: Map[String,SConfigValue]) extends SConfigObject {
    import collection.JavaConverters._
    def asJava: java.util.Map[String,ConfigValue] = map.asJava.asInstanceOf[java.util.Map[String,ConfigValue]]
    override def containsKey(key: scala.Any): Boolean = map.contains(key.toString)
    override def containsValue(value: scala.Any): Boolean = map.values.exists(_ == value)
    override def entrySet(): util.Set[java.util.Map.Entry[String, ConfigValue]] = asJava.entrySet()
    override def get(key: scala.Any): ConfigValue = map(key.toString)
    override def isEmpty: Boolean = map.isEmpty
    override def keySet(): util.Set[String] = asJava.keySet
    override def size(): Int = map.size
    override def values(): util.Collection[ConfigValue] = asJava.values()
    override def unwrapped(): util.Map[String, AnyRef] = asJava.asInstanceOf[java.util.Map[String,AnyRef]]
//    override def toConfig: Config = ???
//    override def withOnlyKey(key: String): ConfigObject = ???
    override def withValue(key: String, value: ConfigValue): SConfigObject = value match {
      case v: SConfigValue =>  copy( map = map + (key -> v) )
      case _ => ???
    }

    override def withPathValue(path: PathSeq, value: SConfigValue): SConfigObject = path match {
      case Nil => this
      case Seq(x) => withValue(x,value)
      case x :: xs => withValue(x, map.get(x) match {
        case Some(obj: SConfigObject) => obj.withPathValue(xs,value)
        case _ => SConfigObject.empty.withPathValue(xs,value)
      })
    }

//    override def withoutKey(key: String): ConfigObject = ???
    def apply(key: String): SConfigValue = map(key)
  }
//  object MapConfigObject {
//    val empty: MapConfigObject = new MapConfigObject(Map())
//
//    def apply(pairs: Iterable[(PathSeq,SConfigValue)]): MapConfigObject =
//      pairs.foldLeft(MapConfigObject.empty)( (obj,p) => obj.withPathValue(p._1,p._2) )
//  }

  val empty: SConfigObject = new MapConfigObject(Map())

  def apply(pairs: Iterable[(PathSeq,SConfigValue)]): SConfigObject =
    pairs.foldLeft(empty)( (obj,p) => obj.withPathValue(p._1,p._2) )
}
