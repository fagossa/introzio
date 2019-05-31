import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "xml-to-json",
    libraryDependencies ++= Seq(
      // effects
      "org.scalaz" %% "scalaz-zio" % "1.0-RC4",
      "org.scalaz" %% "scalaz-zio-streams" % "1.0-RC4",
      // xml
      "com.lucidchart" %% "xtract" % "2.0.1",
      "com.typesafe.play" % "play-json_2.11" % "2.6.7",
      // tests
      scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
