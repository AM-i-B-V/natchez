

val scala212Version = "2.12.19"
val scala213Version = "2.13.13"
val scala30Version = "3.3.3"

val collectionCompatVersion = "2.11.0"

val catsVersion = "2.10.0"
val catsEffectVersion = "3.5.4"
val fs2Version = "3.10.2"

// Publishing

ThisBuild / organization := "com.github.AM-i-B-V"
ThisBuild / licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT")))
ThisBuild / developers := List(
  Developer("tpolecat", "Rob Norris", "rob_norris@mac.com", url("http://www.tpolecat.org"))
)



ThisBuild / githubWorkflowAddedJobs +=
  WorkflowJob(
    id = "docs",
    name = s"Make site",
    scalas = List(scala213Version),
    steps = List(WorkflowStep.CheckoutFull) ++
      WorkflowStep.SetupJava(githubWorkflowJavaVersions.value.toList) ++
      githubWorkflowGeneratedCacheSteps.value ++
      List(WorkflowStep.Sbt(List("docs/makeSite")))
  )

// https://github.com/sbt/sbt/issues/6997
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
ThisBuild / publishMavenStyle := true
Test / publishArtifact := false
ThisBuild / publishTo := Some("GitHub Package Registry" at "https://maven.pkg.github.com/AM-i-B-V/natchez")

ThisBuild / credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  sys.env.getOrElse("GITHUB_ACTOR", ""),
  "PAT"
)

// Headers
lazy val commonSettings = Seq(
  // Testing
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "1.0.0-M11" % Test,
    "org.scalameta" %%% "munit-scalacheck" % "1.0.0-M11" % Test,
    "org.typelevel" %%% "munit-cats-effect" % "2.0.0-M4" % Test,
    "org.typelevel" %%% "scalacheck-effect-munit" % "2.0.0-M2" % Test
  )
)

// Compilation
ThisBuild / scalaVersion := scala213Version
ThisBuild / crossScalaVersions := Seq(scala212Version, scala213Version, scala30Version)
ThisBuild / githubWorkflowScalaVersions := Seq("2.12", "2.13", "3")


lazy val core = crossProject(JVMPlatform)
  .in(file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := "natchez-core",
    description := "Tagless, non-blocking OpenTracing implementation for Scala.",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.typelevel" %%% "cats-effect-kernel" % catsEffectVersion,
      "org.typelevel" %%% "cats-effect" % catsEffectVersion,
      "co.fs2" %%% "fs2-io" % fs2Version,
      "org.typelevel" %%% "case-insensitive" % "1.4.0",
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompatVersion
    )
  )

lazy val xray = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/xray"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "natchez-xray",
    description := "AWS X-Ray bindings implementation",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % "0.14.6",
      "co.fs2" %%% "fs2-io" % fs2Version,
      "com.comcast" %%% "ip4s-core" % "3.5.0",
      "org.scodec" %%% "scodec-bits" % "1.1.38"
    ),
    publishTo := Some("GitHub Package Registry" at "https://maven.pkg.github.com/AM-i-B-V/natchez"),
    credentials += Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      sys.env.getOrElse("GITHUB_ACTOR", ""),
      "PAT"
    )
  )
