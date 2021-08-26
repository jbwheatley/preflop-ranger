name := "preflop-ranger"
organization := "io.github.jbwheatley"
maintainer := "jbwheatley@proton.me"
version := "0.0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "16.0.0-R24"
)
libraryDependencies ++= javaFXModules

enablePlugins(JavaAppPackaging)

// Add JavaFX dependencies
lazy val javaFXModules = {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => "linux"
    case n if n.startsWith("Mac")     => "mac"
    case n if n.startsWith("Windows") => "win"
    case _                            => throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web").map(m =>
    "org.openjfx" % s"javafx-$m" % "16" classifier osName
  )
}

// Fork a new JVM for 'run' and 'test:run' to avoid JavaFX double initialization problems
fork := true

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
Compile / run / mainClass := Some("preflop.ranger.PreflopRanger")

addCommandAlias("packageApp", "universal:packageBin")
