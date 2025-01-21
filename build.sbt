import scala.language.postfixOps

name := "Preflop Ranger"

inThisBuild(
  List(
    organization := "io.github.jbwheatley",
    homepage     := Some(url("https://github.com/jbwheatley/preflop-ranger")),
    developers := List(
      Developer(
        "jbwheatley",
        " ",
        "jbwheatley@proton.me",
        url("https://github.com/jbwheatley")
      )
    ),
    startYear    := Some(2025),
    licenses     := List("GPL-3.0" -> url("https://www.gnu.org/licenses/gpl-3.0")),
    scalaVersion := "2.13.15"
  )
)
version := {
  val v = System.getenv("VERSION")
  if (v == null) "SNAPSHOT" else v
}

val javafxVersion = "23.0.1"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % s"$javafxVersion-R34" excludeAll ExclusionRule("org.openjfx"),
  "com.lihaoyi" %% "upickle" % "4.1.0"
)

libraryDependencies ++= javaFXModules

// Add JavaFX dependencies
lazy val javaFXModules = {
  // Determine OS version of JavaFX binaries
  lazy val osName = (System.getProperty("os.name"), System.getProperty("os.arch")) match {
    case (n, _) if n.startsWith("Linux")       => "linux"
    case (n, "aarch64") if n.startsWith("Mac") => "mac-aarch64"
    case (n, _) if n.startsWith("Mac")         => "mac"
    case (n, _) if n.startsWith("Windows")     => "win"
    case _                                     => throw new Exception("Unknown platform!")
  }
  Seq("media", "controls").map(m =>
    "org.openjfx" % s"javafx-$m" % javafxVersion classifier osName excludeAll ExclusionRule("org.openjfx", s"javafx-$m")
  )
}

// Fork a new JVM for 'run' and 'test:run' to avoid JavaFX double initialization problems
fork := true

Compile / mainClass  := Some("preflop.ranger.PreflopRanger")
assembly / mainClass := Some("preflop.ranger.PreflopRanger")

assembly / assemblyMergeStrategy := {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map { _.toLowerCase }) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case ("substrate" :: xs) => MergeStrategy.discard
      case _                   => MergeStrategy.deduplicate
    }
  case "module-info.class" => MergeStrategy.concat
  case _                   => MergeStrategy.deduplicate
}

scalacOptions += "-deprecation"
