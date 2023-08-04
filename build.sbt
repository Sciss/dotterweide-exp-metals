lazy val baseName           = "Dotterweide-Exp-Metals"
lazy val baseNameL          = baseName.toLowerCase
lazy val projectVersion     = "0.1.0-SNAPSHOT"
lazy val mimaVersion        = "0.1.0"

// versions of library dependencies
val deps = new {
  val main = new {
    val dotterweide = "0.4.3"
    val fileUtil    = "1.1.5"
    val lsp4j       = "0.21.0"
    val scalaSwing  = "3.0.0"
    val scopt       = "4.1.0"
    val tm4e        = "0.1.0"
  }
  val test = new {
    val scalaTest   = "3.2.16"
  }
}

lazy val commonSettings = Seq(
  version             := projectVersion,
  description         := "Dotterweide experiments in using the Metals language server",
  homepage            := Some(url(s"https://github.com/dotterweide/$baseNameL")),
  scalaVersion        := "2.13.11",
  crossScalaVersions  := Seq("2.13.11", "2.12.14"),
  licenses            := Seq(lgpl2),
  scalacOptions      ++= Seq(
    "-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13"
  )
)

lazy val testSettings = Seq(
  libraryDependencies += {
    "org.scalatest" %% "scalatest" % deps.test.scalaTest % Test
  }
)

lazy val lgpl2 = "LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt")

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(testSettings)
  .settings(
    name := baseName,
    Compile / run / mainClass := Some("dotterweide.metals.Demo"),
    libraryDependencies ++= Seq(
      "de.sciss"                %%  "dotterweide-ui"      % deps.main.dotterweide,
      "de.sciss"                %%  "dotterweide-scala"   % deps.main.dotterweide,
      "de.sciss"                %%  "fileutil"            % deps.main.fileUtil,     // extension methods for files
      "com.github.scopt"        %%  "scopt"               % deps.main.scopt,        // command line parsing
      "org.scala-lang.modules"  %%  "scala-swing"         % deps.main.scalaSwing,   // UI
      "org.eclipse.lsp4j"       %   "org.eclipse.lsp4j"   % deps.main.lsp4j,        // language client
//      "dev.secondsun"           % "org.eclipse.tm4e.core" % "0.4.8",              // testing text-mate grammar parser
      "de.sciss"                % "tm4e-core"             % deps.main.tm4e,         // testing text-mate grammar parser
    )
  )


