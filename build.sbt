
name := "scala-gntp"

organization := "fr.ramiro"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  //"org.slf4j" % "slf4j-api" % "1.7.23",
  // "org.slf4j" % "slf4j-nop" % "1.7.23" % Test,
  "org.scalatest" %% "scalatest" % "3.0.0" % Test
)

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepTask(PgpKeys.publishSigned),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand(xerial.sbt.Sonatype.SonatypeCommand.sonatypeReleaseAll),
  pushChanges
)