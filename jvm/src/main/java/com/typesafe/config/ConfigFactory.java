package com.typesafe.config;

import sconfig.JvmSConfigFactory$;

public final class ConfigFactory {

  public static Config load() {
    return JvmSConfigFactory$.MODULE$.load();
  }
}
