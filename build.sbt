
name := "scala-growl"

organization := "fr.ramiro"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "org.slf4j" % "slf4j-nop" % "1.7.30" % Test,
  "org.scalatest" %% "scalatest" % "3.2.6" % Test
)

(compile in Compile) := ((compile in Compile) dependsOn (scalastyle in Compile).toTask("")).value

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepTask(PgpKeys.publishSigned), //releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

Global / onChangedBuildSource := ReloadOnSourceChanges
