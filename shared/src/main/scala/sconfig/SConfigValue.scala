//     Project: sconfig
//      Module: shared
// Description: sconfig implementation of ConfigValue
package sconfig

import com.typesafe.config
import com.typesafe.config.ConfigException.WrongType
import com.typesafe.config._
import sconfig.SConfigValue.TypeException

trait SConfigValue extends ConfigValue {
  override def atKey(key: String) = ???
  override def atPath(path: String) = ???
  override def origin() = null
  override def render(): HoconString = ???
  override def render(options: ConfigRenderOptions) = ???
  override def withFallback(other: ConfigMergeable) = this
  override def withOrigin(origin: ConfigOrigin) = ???
  def asInt: Int = throw TypeException("NUMBER",valueType().toString)
  def asLong: Long = throw TypeException("NUMBER",valueType().toString)
  def asFloat: Float = throw TypeException("NUMBER",valueType().toString)
  def asDouble: Double = throw TypeException("NUMBER",valueType().toString)
  def asBoolean: Boolean = throw TypeException("BOOL",valueType().toString)
  def asString: String = render()
  def asObject: SConfigObject = ???
}

object SConfigValue {

  case class TypeException(expected: String, actual: String) extends RuntimeException()

  def apply(value: Any): SConfigValue = value match {
    case null => NullValue
    case i:Int => LongValue(i)
    case l:Long => LongValue(l)
    case f:Float => FloatValue(f)
    case d:Double => DoubleValue(d)
    case b:Boolean => if(b) TrueValue else FalseValue
    case s:String => StringValue(s)
    case t => throw new RuntimeException(s"Unsupported type for SConfigValue: $t")
  }
  object NullValue extends SConfigValue {
    override def valueType(): ConfigValueType = ConfigValueType.NULL
    override def unwrapped(): AnyRef = null
  }

  case class StringValue(s: String, quoted: Boolean = true) extends SConfigValue {
    override def valueType() = ConfigValueType.STRING
    override def unwrapped() = s
    override def asString = s
  }

  trait BooleanValue extends SConfigValue {
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

  trait NumberValue extends SConfigValue {
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
    override def asFloat: Float = l.toFloat
    override def asDouble: Double = l.toDouble
  }

  case class FloatValue(f: Float) extends NumberValue {
    override def unwrapped(): AnyRef = java.lang.Float.valueOf(f)

    override def render() = f.toString
    override def asInt: Int = f.toInt
    override def asLong: Long = f.toLong
    override def asFloat: Float = f
    override def asDouble: Double = f.toDouble
  }

  case class DoubleValue(d: Double) extends NumberValue {
    override def unwrapped(): AnyRef = java.lang.Double.valueOf(d)

    override def render() = d.toString
    override def asInt: Int = d.toInt
    override def asLong: Long = d.toLong
    override def asFloat: Float = d.toFloat
    override def asDouble: Double = d
  }

}
