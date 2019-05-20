ThisBuild / organization := "dev.zerosum"
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val commonSettings = Seq (
  resolvers ++= Seq(

  ),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.22",
    "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
)


lazy val root = project in file(".")

lazy val receiver = (project in file("receiver"))
  .settings(
    commonSettings
  )

lazy val transferer = (project in file("transferer"))
  .settings(
    commonSettings
  )

lazy val deliverer = (project in file("deliverer"))
  .settings(
    commonSettings
  )
