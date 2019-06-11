scalaVersion := "2.12.8"
version := "0.1.0"
organization := "com.example"
organizationName := "example"
name := "scraper"

resolvers += Resolver.sonatypeRepo("snapshots")

val CatsVersion = "1.6.1"
val Http4sVersion = "0.20.1"
val CirceVersion = "0.11.1"
//val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"

val SangriaVersion = "1.4.2"
val SangriaCirceVersion = "1.2.1"
val ScalaTestVersion = "3.0.5"

val KindProjectorVersion = "0.9.9"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % CatsVersion,
  "org.sangria-graphql" %% "sangria" % SangriaVersion,
  "org.sangria-graphql" %% "sangria-circe" % SangriaCirceVersion,
  //  "io.circe" %% "circe-optics" % "0.9.3",
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  //"org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  //"org.specs2"      %% "specs2-core"         % Specs2Version % "test",
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,

  "io.circe" %% "circe-optics" % "0.9.3" // Http4sServer
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % KindProjectorVersion cross CrossVersion.binary)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
)
