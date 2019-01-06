//     Project: sconfig
//      Module: native
// Description: scala-native implementation of ConfigFactory
package com.typesafe.config

import java.io.{BufferedInputStream, File, FileInputStream, IOException}

import uconfig.{UConfig, UConfigFactory}

object ConfigFactory extends UConfigFactory {

  /**
   * Parses a file into a Config instance, using the default parse options.
   *
   * @param file File to be parsed
   * @return parsed configuration
   * @throws ConfigException on IO or parse errors
   */
  def parseFile(file: File): UConfig = try{
    val bin = new BufferedInputStream(new FileInputStream(file))
    parseInputStream(bin)
  } catch {
    case io: IOException => throw new ConfigException.IO(null,io.getMessage,io)
    case x: Throwable => throw x
  }
}
