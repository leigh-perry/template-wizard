// sbt dependencyUpdates

// groupId
organization := "com.github.leigh-perry"

// forms artifactId, together with scalaBinaryVersion
name := "template-wizard"
version := "1.0.1-SNAPSHOT"

scalaVersion := "2.12.2"
scalacOptions := Seq("-unchecked", "-deprecation", "-Xexperimental", "-feature")

val VersionTwitterUtil = "6.43.0"
val VersionHocon = "1.3.1"
val VersionFs2 = "0.9.6"

val VersionMockito = "2.7.22"
val VersionScalatest = "3.0.3"
val VersionScalacheck = "1.13.5"

// https://github.com/twitter/util
libraryDependencies += "com.twitter" %% "util-core" % VersionTwitterUtil
libraryDependencies += "com.twitter" %% "util-app" % VersionTwitterUtil

libraryDependencies += "co.fs2" %% "fs2-io" % VersionFs2

// test
libraryDependencies += "org.mockito" % "mockito-core" % VersionMockito % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % VersionScalatest % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % VersionScalacheck % "test"

// publication
pomIncludeRepository := { _ => false }
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
homepage := Some(url("https://github.com/leigh-perry/template-wizard"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/leigh-perry/template-wizard"),
    "scm:git@github.com:leigh-perry/template-wizard.git"
  )
)

developers := List(
  Developer(
    id    = "leigh-perry",
    name  = "Leigh Perry",
    email = "lperry.breakpoint@gmail.com",
    url   = url("https://github.com/leigh-perry")
  )
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some(/*"snapshots"*/ "[inhibited]" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false
