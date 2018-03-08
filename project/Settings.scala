import sbt.Compile
import sbt.Keys.{console, scalaVersion, scalacOptions}

object Settings {
  val settings = Seq(
    scalaVersion := "2.12.4",
    scalacOptions := Seq(
      "-unchecked",
      "-feature",
      "-deprecation",
      "-encoding",
      "utf8",
      //        "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:unsound-match", // Pattern match may not be typesafe.
      "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
    ),
    scalacOptions in (Compile, console) ~= (_.filterNot(
      Set(
        "-Ywarn-unused:imports",
        "-Xfatal-warnings"
      )))
  )
}
