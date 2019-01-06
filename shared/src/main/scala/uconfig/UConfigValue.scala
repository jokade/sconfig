//     Project: sconfig
//      Module: shared
// Description: sconfig implementation of ConfigValue
package uconfig

import com.typesafe.config
import com.typesafe.config.ConfigException.WrongType
import com.typesafe.config._
import uconfig.UConfigValue.TypeException
import collection.JavaConverters._

trait UConfigValue extends ConfigValue {

  override def atKey(key: String) = ???
  override def atPath(path: String) = ???
  override def origin() = null
  override def render(): HoconString = ???
  override def render(options: ConfigRenderOptions) = ???
  override def withFallback(other: ConfigMergeable) = this
  override def withOrigin(origin: ConfigOrigin) = ???
  def asInt: java.lang.Integer = throw TypeException("NUMBER",valueType().toString)
  def asLong: java.lang.Long = throw TypeException("NUMBER",valueType().toString)
  def asFloat: java.lang.Float = throw TypeException("NUMBER",valueType().toString)
  def asDouble: java.lang.Double = throw TypeException("NUMBER",valueType().toString)
  def asBoolean: java.lang.Boolean = throw TypeException("BOOL",valueType().toString)
  def asString: String = render()
  def asIntSeq: Seq[java.lang.Integer] = throw TypeException("LIST",valueType().toString)
  def asIntList: java.util.List[java.lang.Integer] = asIntSeq.asJava
  def asLongSeq: Seq[java.lang.Long] = throw TypeException("LIST",valueType().toString)
  def asLongList: java.util.List[java.lang.Long] = asLongSeq.asJava
  def asFloatSeq: Seq[java.lang.Float] = throw TypeException("LIST",valueType().toString)
  def asFloatList: java.util.List[java.lang.Float] = asFloatSeq.asJava
  def asDoubleSeq: Seq[java.lang.Double] = throw TypeException("LIST",valueType().toString)
  def asDoubleList: java.util.List[java.lang.Double] = asDoubleSeq.asJava
  def asBooleanSeq: Seq[java.lang.Boolean] = throw TypeException("LIST",valueType().toString)
  def asBooleanList: java.util.List[java.lang.Boolean] = asBooleanSeq.asJava
  def asStringSeq: Seq[String] = throw TypeException("LIST",valueType().toString)
  def asStringList: java.util.List[String] = asStringSeq.asJava
  def asObject: UConfigObject = ???
}

object UConfigValue {

  case class TypeException(expected: String, actual: String) extends RuntimeException()

  trait AtomicValue extends UConfigValue

  def apply(value: Any): UConfigValue = value match {
    case null => NullValue
    case i:Int => LongValue(i)
    case l:Long => LongValue(l)
    case f:Float => FloatValue(f)
    case d:Double => DoubleValue(d)
    case b:Boolean => if(b) TrueValue else FalseValue
    case s:String => StringValue(s)
    case t => throw new RuntimeException(s"Unsupported type for SConfigValue: $t")
  }
  object NullValue extends AtomicValue {
    override def valueType(): ConfigValueType = ConfigValueType.NULL
    override def unwrapped(): AnyRef = null
  }

  case class StringValue(s: String, quoted: Boolean = true) extends AtomicValue {
    override def valueType() = ConfigValueType.STRING
    override def unwrapped() = s
    override def asString = s
  }

  trait BooleanValue extends AtomicValue {
    override def valueType(): ConfigValueType = ConfigValueType.BOOLEAN
  }

  case object TrueValue extends BooleanValue {
    override def unwrapped(): AnyRef = java.lang.Boolean.TRUE
    override def asBoolean = true
    override def render() = "true"
  }

  case object FalseValue extends BooleanValue {
    override def unwrapped(): AnyRef = java.lang.Boolean.FALSE
    override def asBoolean = false
    override def render() = "false"
  }

  trait NumberValue extends AtomicValue {
    override def valueType(): ConfigValueType = ConfigValueType.NUMBER
  }

  object NumberValue {
    def apply(s: String): NumberValue =
    // TODO: better method for float detection (handle during parsing?)
      if(s.contains(".") | s.contains("e") | s.contains("E"))
        DoubleValue(s.toDouble)
      else
        LongValue(s.toLong)
  }

  case class LongValue(l: Long) extends NumberValue {
    override def unwrapped(): AnyRef = java.lang.Long.valueOf(l)
    override def render() = l.toString
    override def asInt = l.toInt
    override def asLong = l
    override def asFloat = l.toFloat
    override def asDouble = l.toDouble
  }

  case class FloatValue(f: Float) extends NumberValue {
    override def unwrapped(): AnyRef = java.lang.Float.valueOf(f)

    override def render() = f.toString
    override def asInt = f.toInt
    override def asLong = f.toLong
    override def asFloat = f
    override def asDouble = f.toDouble
  }

  case class DoubleValue(d: Double) extends NumberValue {
    override def unwrapped(): AnyRef = java.lang.Double.valueOf(d)

    override def render() = d.toString
    override def asInt = d.toInt
    override def asLong = d.toLong
    override def asFloat = d.toFloat
    override def asDouble = d
  }

  case class ListValue(list: Seq[AtomicValue]) extends UConfigValue {
    override def unwrapped(): java.util.List[AnyRef] = list.map(_.unwrapped).asJava

    override def valueType(): ConfigValueType = ConfigValueType.LIST

    override def asBooleanSeq = list.map(_.asBoolean)
    override def asIntSeq = list.map(_.asInt)
    override def asLongSeq = list.map(_.asLong)
    override def asFloatSeq = list.map(_.asFloat)
    override def asDoubleSeq = list.map(_.asDouble)
    override def asStringSeq = list.map(_.asString)
  }
}
