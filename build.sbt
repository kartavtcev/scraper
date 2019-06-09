ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
lazy val root = (project in file("."))
  .settings(
    name := "scraper",
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )