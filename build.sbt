name := "backend"
organization := "com.example"
version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      guice,
      // Slick dependencies
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "org.postgresql" % "postgresql" % "42.2.23",
      // Testing dependencies
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test, // For Play-specific tests
      "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % Test, // For mocking
      "org.fusesource.jansi" % "jansi" % "2.4.0"
    ),
    dependencyOverrides += "com.google.inject" % "guice" % "5.1.0"
  )

// Adds additional packages into Twirl
// TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
