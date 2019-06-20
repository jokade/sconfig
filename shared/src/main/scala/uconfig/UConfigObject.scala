//     Project: sconfig
//      Module: shared
// Description: sconfig implementation of ConfigObject
package uconfig

import java.util

import com.typesafe.config._

import scala.annotation.tailrec
import scala.collection.MapLike

trait UConfigObject extends UConfigValue with ConfigObject {
  override def clear(): Unit = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def put(key: String, value: ConfigValue): ConfigValue = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def putAll(m: util.Map[_ <: String, _ <: ConfigValue]): Unit = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def remove(key: scala.Any): ConfigValue = throw new UnsupportedOperationException("ConfigObject is immutable")
  override def valueType(): ConfigValueType = ConfigValueType.OBJECT
  override def asObject: UConfigObject = this


  def apply(key: String): UConfigValue
  def opt(key: String): Option[UConfigValue]
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
  def obj(key: String): UConfigObject = apply(key).asObject

  def withPathValue(path: PathSeq, value: UConfigValue): UConfigObject

  def find(path: Path): Option[UConfigValue] = find(PathSeq.fromString(path))
  def find(path: PathSeq): Option[UConfigValue]

  def pairs: Iterable[(String,UConfigValue)]
  def keys: Iterable[String]
}

object UConfigObject {

  case class MapConfigObject(map: Map[String,UConfigValue]) extends UConfigObject {
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
    override def pairs: Iterable[(String, UConfigValue)] = map
    override def keys: Iterable[String] = map.keys

    override def withValue(key: String, value: ConfigValue): UConfigObject = value match {
      case svalue: UConfigValue =>
        val newValue: UConfigValue = map.get(key) match {
          case Some(v) => svalue.withFallback(v)
          case _ => svalue
        }
        copy( map = map + (key -> newValue) )
    }

    override def withFallback(other: ConfigMergeable): UConfigObject = other match {
      case obj: UConfigObject =>
        val pairsWithFallback = pairs
          .map( kv => (obj.opt(kv._1),kv) )
          .map{
            case (Some(fallback),(key,value)) => (key,value.withFallback(fallback))
            case (_,kv) => kv
          }
        UConfigObject.MapConfigObject( (obj.pairs ++ pairsWithFallback).toMap )
      case UConfig(root) => withFallback(root)
      case x => this
    }

    override def withPathValue(path: PathSeq, value: UConfigValue): UConfigObject = path match {
      case Nil => this
      case x :: Nil => withValue(x,value)
      case x :: xs => withValue(x, map.get(x) match {
        case Some(obj: UConfigObject) =>
          obj.withPathValue(xs,value)
        case _ =>
          UConfigObject.empty.withPathValue(xs,value)
      })
    }

    def apply(key: String): UConfigValue = map(key)
    def opt(key: String): Option[UConfigValue] = map.get(key)

    override def find(path: PathSeq): Option[UConfigValue] = {
      @tailrec
      def loop(path: PathSeq, obj: UConfigObject): Option[UConfigValue] = path match {
        case Nil => Some(obj)
        case x::Nil => obj.opt(x)
        case x::xs => loop(xs,obj.obj(x))
      }
      loop(path,this)
    }
  }

  case class CompoundConfig(first: UConfigObject, next: UConfigObject) extends UConfigObject {
    def apply(key: String): UConfigValue = first.opt(key).getOrElse(next(key))

    override def opt(key: String): Option[UConfigValue] = first.opt(key) match {
      case value@ Some(_) => value
      case _ => next.opt(key)
    }

    override def withFallback(other: ConfigMergeable)  = next.withFallback(other) match {
      case obj: UConfigObject => CompoundConfig(first,obj)
    }

    override def withPathValue(path: PathSeq, value: UConfigValue): UConfigObject = ???

    override def find(path: PathSeq): Option[UConfigValue] = first.find(path) match {
      case value@ Some(_) => value
      case _ => next.find(path)
    }

    override def pairs: Iterable[(String, UConfigValue)] = next.pairs.toMap ++ first.pairs

    override def keys: Iterable[String] = first.keys.toSet ++ next.keys

    override def withValue(key: Key, value: ConfigValue): ConfigValue = ???

    override def unwrapped(): AnyRef = ???

    override def size(): Int = keys.size

    override def isEmpty: Boolean = first.isEmpty && next.isEmpty

    override def containsKey(key: scala.Any): Boolean = first.containsKey(key) || next.containsKey(key)

    override def containsValue(value: scala.Any): Boolean = ???

    override def get(key: scala.Any): ConfigValue = apply(key.toString)

    override def keySet(): util.Set[String] = ???

    override def values(): util.Collection[ConfigValue] = ???

    override def entrySet(): util.Set[util.Map.Entry[String, ConfigValue]] = ???
  }

  val empty: UConfigObject = new MapConfigObject(Map())

  def apply(pairs: Iterable[(PathSeq,UConfigValue)]): UConfigObject =
    pairs.foldLeft(empty)( (obj,p) => obj.withPathValue(p._1,p._2) )

  def fromMap(pairs: Iterable[(String,Any)]): UConfigObject = apply( pairs.map( p => (PathSeq.fromString(p._1), UConfigValue(p._2)) ) )
}
