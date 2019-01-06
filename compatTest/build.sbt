val sconfigVersion = "0.0.1-SNAPSHOT"

val sharedSettings = Seq(
  scalaVersion := "2.11.12"
)

lazy val lib = project
  .settings(sharedSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "de.surfice" %% "sconfig" % sconfigVersion % "provided"
    )
  )

lazy val compatTest = project.in(file("."))
  .dependsOn(lib)
  .settings(sharedSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.3",
      "com.lihaoyi" %% "utest" % "0.6.3" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
