// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

val Versions = new {
  val fastparse = "1.0.0"
  val utest     = "0.6.3"

  val scala211  = "2.11.12"
  val scala212  = "2.12.3"
}

lazy val commonSettings = Seq(
  organization := "de.surfice",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := Versions.scala211,
  scalacOptions ++= Seq("-deprecation","-unchecked","-feature","-Xlint")
  //crossScalaVersions := Seq("2.11.11", "2.12.2")
)

lazy val root = project.in(file(".")).
  aggregate(sconfigJVM,sconfigJS,sconfigNative).
  settings(commonSettings ++ dontPublish:_*).
  //settings(sonatypeSettings: _*).
  settings(
    name := "sconfig"
  )


lazy val sconfig = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(commonSettings ++ publishingSettings:_*)
  .settings(
    name := "sconfig",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % Versions.fastparse,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.lihaoyi" %%% "utest" % Versions.utest % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jvmSettings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212)
  )
  .jsSettings(
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212)
    //preLinkJSEnv := NodeJSEnv().value,
    //postLinkJSEnv := NodeJSEnv().value
  )
  .nativeSettings(
    nativeLinkStubs := true
  )

lazy val sconfigJVM    = sconfig.jvm
lazy val sconfigJS     = sconfig.js
lazy val sconfigNative = sconfig.native

lazy val dontPublish = Seq(
  publish := {},
  publishLocal := {},
  com.typesafe.sbt.pgp.PgpKeys.publishSigned := {},
  com.typesafe.sbt.pgp.PgpKeys.publishLocalSigned := {},
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository",file("target/unusedrepo")))
)


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
 
