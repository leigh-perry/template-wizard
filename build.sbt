// sbt dependencyUpdates

name := "template-wizard"
version := "0.1.0"
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
