// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

lazy val commonSettings = Seq(
  organization := "de.surfice",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq("-deprecation","-unchecked","-feature","-Xlint")
  //crossScalaVersions := Seq("2.11.11", "2.12.2")
)

lazy val root = project.in(file(".")).
  aggregate(sconfigJVM,sconfigJS,sconfigNative).
  settings(commonSettings:_*).
  //settings(sonatypeSettings: _*).
  settings(
    name := "sconfig",
    publish := {},
    publishLocal := {},
    resolvers += Resolver.sonatypeRepo("releases")
  )


lazy val sconfig = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(commonSettings:_*)
  .settings(publishingSettings:_*)
  .settings(
    name := "sconfig",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "0.4.4",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.lihaoyi" %%% "utest" % "0.4.8" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jvmSettings(
    crossScalaVersions := Seq("2.11.11", "2.12.2")
  )
  .jsSettings(
    crossScalaVersions := Seq("2.11.11", "2.12.2")
    //preLinkJSEnv := NodeJSEnv().value,
    //postLinkJSEnv := NodeJSEnv().value
  )

lazy val sconfigJVM    = sconfig.jvm
lazy val sconfigJS     = sconfig.js
lazy val sconfigNative = sconfig.native


lazy val publishingSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <url>https://github.com/jokade/sconfig</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:jokade/sconfig</url>
      <connection>scm:git:git@github.com:jokade/sconfig.git</connection>
    </scm>
    <developers>
      <developer>
        <id>jokade</id>
        <name>Johannes Kastner</name>
        <url>https://github.com/jokade</url>
      </developer>
    </developers>
  )
)
 
