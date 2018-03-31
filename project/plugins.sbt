addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.19")

addSbtPlugin("org.portable-scala" % "sbt-crossproject" % "0.3.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.3.1")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.3.6")

//addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.5.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// library for plugin testing
libraryDependencies <+= (sbtVersion) { sv =>
  "org.scala-sbt" % "scripted-plugin" % sv
}
