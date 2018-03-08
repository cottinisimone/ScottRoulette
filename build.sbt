val mainClassName = "com.scott.roulette.Main"

lazy val root = project
  .in(file("."))
  .settings(
    libraryDependencies ++= (Dependencies.allDeps ++ Dependencies.testDeps),
    mainClass in (Compile, run) := Some(mainClassName),
    mainClass in (Compile, packageBin) := Some(mainClassName),
    name := "chat-roulette",
    organization := "com.scott",
    version := "0.0.1",
    parallelExecution in test := false,
    scalafmtOnCompile in Compile := true
  )
  .settings(Settings.settings: _*)
