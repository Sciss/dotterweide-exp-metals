lazy val baseName           = "Dotterweide-Exp-Metals"
lazy val baseNameL          = baseName.toLowerCase
lazy val projectVersion     = "0.1.0-SNAPSHOT"
lazy val mimaVersion        = "0.1.0"

// versions of library dependencies
val deps = new {
  val main = new {
    val dotterweide = "0.1.2-SNAPSHOT"
    val fileUtil    = "1.1.3"
    val lsp4j       = "0.7.0"
    val scalaSwing  = "2.1.0"
    val scopt       = "3.7.1"
  }
  val test = new {
    val scalaTest   = "3.0.7"
  }
}

lazy val commonSettings = Seq(
  version             := projectVersion,
  description         := "Dotterweide experiments in using the Metals language server",
  homepage            := Some(url(s"https://github.com/dotterweide/$baseNameL")),
  scalaVersion        := "2.12.8",
  crossScalaVersions  := Seq("2.12.8", "2.11.12"),
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
    mainClass in (Compile, run) := Some("dotterweide.DownloadAndBrowseDocs"),
    libraryDependencies ++= Seq(
      "de.sciss"                %%  "dotterweide-ui"    % deps.main.dotterweide,
      "de.sciss"                %%  "dotterweide-scala" % deps.main.dotterweide,
      "de.sciss"                %%  "fileutil"          % deps.main.fileUtil,     // extension methods for files
      "com.github.scopt"        %%  "scopt"             % deps.main.scopt,        // command line parsing
      "org.scala-lang.modules"  %%  "scala-swing"       % deps.main.scalaSwing,   // UI
      "org.eclipse.lsp4j"       %   "org.eclipse.lsp4j" % deps.main.lsp4j         // language client
    )
  )


