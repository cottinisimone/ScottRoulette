import sbt._

object Dependencies {

  private val circeVersion: String = "0.9.1"
  private val akkaVersion: String = "10.0.11"
  private val pureConfigVersion: String = "0.9.0"

  lazy val allDeps: Seq[ModuleID] = akkaDeps ++ circeDeps ++ pureConfigDeps

  lazy val akkaDeps: Seq[ModuleID] = Seq("com.typesafe.akka" %% "akka-http" % akkaVersion)
  lazy val circeDeps: Seq[ModuleID] = Seq("circe-core", "circe-generic", "circe-parser").map("io.circe" %% _ % circeVersion)
  lazy val pureConfigDeps: Seq[ModuleID] = Seq("com.github.pureconfig" %% "pureconfig" % pureConfigVersion)

  lazy val testDeps: Seq[ModuleID] = Seq()
}
