//     Project: sconfig
//      Module: shared
// Description: Exceptions specific to Config
package com.typesafe.config

class ConfigException(val origin: ConfigOrigin, message: String, cause: Throwable)
  extends RuntimeException(if(origin==null) message else s"$origin: $message",cause) {
  def this(origin: ConfigOrigin, message: String) = this(origin,message,null)
  def this(message: String, cause: Throwable) = this(null,message,cause)
  def this(message: String) = this(null,message,null)
}

object ConfigException {
  class WrongType(origin: ConfigOrigin, message: String, cause: Throwable)
    extends ConfigException(origin,message,cause) {
    def this(origin: ConfigOrigin, path: Path, expected: String, actual: String, cause: Throwable) =
      this(origin,s"$path has type $actual rather than $expected",cause)
    def this(origin: ConfigOrigin, path: Path, expected: String, actual: String) =
      this(origin,path,expected,actual,null)
    def this(origin: ConfigOrigin, message: String) = this(origin,message,null)
  }

  class Missing(path: String, cause: Throwable)
    extends ConfigException(s"No configuration setting found for key '$path'",cause) {
    def this(path: String) = this(path,null)
  }

  class BadPath(origin: ConfigOrigin, path: String, message: String, cause: Throwable)
    extends ConfigException(origin, if(path!=null) s"Invalid path '$path': $message" else message, cause) {
    def this(path: String, message: String) = this(null,path,message,null)
  }

  class Parse(origin: ConfigOrigin, message: String, cause: Throwable)
    extends ConfigException(origin,message,cause) {
    def this(message: String) = this(null,message,null)
    def this(origin: ConfigOrigin, message: String) = this(origin,message,null)
  }
}
