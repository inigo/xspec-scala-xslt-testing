lazy val scala212 = "2.12.17"
lazy val scala213 = "2.13.10"
lazy val supportedScalaVersions = List(scala213, scala212)

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := scala213
ThisBuild / organization := "net.surguy.xspec"
ThisBuild / organizationName := "67 Bricks"
ThisBuild / organizationHomepage := Some(url("https://67bricks.com"))
ThisBuild / versionScheme := Some("semver-spec")
ThisBuild / scalacOptions ++= Seq("-deprecation", "-unchecked")

lazy val root = (project in file("."))
  .settings(
    name := "specs2-xspec",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "io.xspec" % "xspec" % "2.2.4",
      "org.specs2" %% "specs2-core" % "4.20.0",
      "org.specs2" %% "specs2-matcher-extra" % "4.20.0",
      "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
    )
  )

// Release settings
releaseCrossBuild := true

// Publish to AWS S3
// You will need to have credentials set up in your ~/.aws/credentials file to do this
awsProfile := Option(System.getProperty("s3.profile"))

s3region := com.amazonaws.regions.Regions.EU_WEST_1

publishMavenStyle := false

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value(s"67 Bricks $prefix repo", s3(prefix + ".sixtysevenbricks.com")).withIvyPatterns)
}

s3acl := {
  if (isSnapshot.value) Some(com.amazonaws.services.s3.model.CannedAccessControlList.Private)
  else Some(com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead)
}
