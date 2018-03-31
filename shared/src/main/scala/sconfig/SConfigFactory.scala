//     Project: sconfig
//      Module:
// Description:
package sconfig

import java.io.{InputStream, Reader}

abstract class SConfigFactory {

  /**
   * Returns an empty configuration.
   */
  def empty: SConfig = SConfig.empty

  /**
   * Parses the provided HOCON string.
   *
   * @param config
   */
  def parseString(config: String): SConfig = SConfig(config)

  /**
   * Parses the contents of an input stream into a Config instance, using the default parse options.
   *
   * @param in Stream to parse
   * @return parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseInputStream(in: InputStream): SConfig = {
    val configString = io.Source.fromInputStream(in).getLines().mkString("\n")
    parseString(configString)
  }

  /**
   * Loads a default configuration.
   */
  def load(): SConfig = ???
}

