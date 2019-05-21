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

lazy val message = (project in file("message"))
  .settings(
    libraryDependencies ++= {
      val circeVersion = "0.10.0"

      Seq(
        "io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % circeVersion)
    }
  )

lazy val receiver = (project in file("receiver"))
  .dependsOn(message)
  .settings(
    commonSettings
  )

lazy val transferer = (project in file("transferer"))
  .dependsOn(message)
  .settings(
    commonSettings
  )

lazy val deliverer = (project in file("deliverer"))
  .dependsOn(message)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.simplejavamail" % "simple-java-mail" % "5.1.6"
    )
  )
