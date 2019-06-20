addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "0.6.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "0.6.23")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.0-M2")

//addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.5.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// library for plugin testing
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
