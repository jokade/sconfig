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
  def opt(key: String): Option[SConfigValue]
  def int(key: String): Int = apply(key).asInt
  def ints(key: String): Seq[java.lang.Integer] = apply(key).asIntSeq
  def long(key: String): Long = apply(key).asLong
  def longs(key: String): Seq[java.lang.Long] = apply(key).asLongSeq
  def float(key: String): Float = apply(key).asFloat
  def floats(key: String): Seq[java.lang.Float] = apply(key).asFloatSeq
  def double(key: String): Double = apply(key).asDouble
  def doubles(key: String): Seq[java.lang.Double] = apply(key).asDoubleSeq
  def boolean(key: String): Boolean = apply(key).asBoolean
  def booleans(key: String): Seq[java.lang.Boolean] = apply(key).asBooleanSeq
  def string(key: String): String = apply(key).asString
  def strings(key: String): Seq[String] = apply(key).asStringSeq
  def obj(key: String): SConfigObject = apply(key).asObject

  def withPathValue(path: PathSeq, value: SConfigValue): SConfigObject

  def find(path: Path): Option[SConfigValue] = find(PathSeq.fromString(path))
  def find(path: PathSeq): Option[SConfigValue]

  def pairs: Iterable[(String,SConfigValue)]
  def keys: Iterable[String]
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
    override def pairs: Iterable[(String, SConfigValue)] = map
    override def keys: Iterable[String] = map.keys

    override def withValue(key: String, value: ConfigValue): SConfigObject = value match {
      case svalue: SConfigValue =>
        val newValue: SConfigValue = map.get(key) match {
          case Some(v) => svalue.withFallback(v)
          case _ => svalue
        }
        copy( map = map + (key -> newValue) )
    }

    override def withFallback(other: ConfigMergeable): SConfigValue = other match {
      case obj: SConfigObject =>
        val pairsWithFallback = pairs
          .map( kv => (obj.opt(kv._1),kv) )
          .map{
            case (Some(fallback),(key,value)) => (key,value.withFallback(fallback))
            case (_,kv) => kv
          }
        SConfigObject.MapConfigObject( (obj.pairs ++ pairsWithFallback).toMap )
      case _ => this
    }

    override def withPathValue(path: PathSeq, value: SConfigValue): SConfigObject = path match {
      case Nil => this
      case x :: Nil => withValue(x,value)
      case x :: xs => withValue(x, map.get(x) match {
        case Some(obj: SConfigObject) =>
          obj.withPathValue(xs,value)
        case _ =>
          SConfigObject.empty.withPathValue(xs,value)
      })
    }

    def apply(key: String): SConfigValue = map(key)
    def opt(key: String): Option[SConfigValue] = map.get(key)

    override def find(path: PathSeq): Option[SConfigValue] = {
      @tailrec
      def loop(path: PathSeq, obj: SConfigObject): Option[SConfigValue] = path match {
        case Nil => Some(obj)
        case x::Nil => obj.opt(x)
        case x::xs => loop(xs,obj.obj(x))
      }
      loop(path,this)
    }
  }

  val empty: SConfigObject = new MapConfigObject(Map())

  def apply(pairs: Iterable[(PathSeq,SConfigValue)]): SConfigObject =
    pairs.foldLeft(empty)( (obj,p) => obj.withPathValue(p._1,p._2) )
}
