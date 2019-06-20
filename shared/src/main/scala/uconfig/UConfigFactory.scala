//     Project: sconfig
//      Module:
// Description:
package uconfig

import java.io.{InputStream, Reader}

abstract class UConfigFactory {

  /**
   * Returns an empty configuration.
   */
  def empty: UConfig = UConfig.empty

  /**
   * Parses the provided HOCON string.
   *
   * @param config
   */
  def parseString(config: String): UConfig = UConfig(config)

  /**
   * Parses the contents of an input stream into a Config instance, using the default parse options.
   *
   * @param in Stream to parse
   * @return parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseInputStream(in: InputStream): UConfig = {
    val configString = io.Source.fromInputStream(in).getLines().mkString("\n")
    parseString(configString)
  }

  /**
   * Loads a default configuration.
   */
  def load(): UConfig = ???

  def fromMap(pairs: (String,Any)*): UConfig = fromMap(pairs)
  def fromMap(pairs: Iterable[(String,Any)]): UConfig = UConfig(UConfigObject.fromMap(pairs))
}

