/*
 *  LanguageClientTest.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package dotterweide.metals

import java.awt.Dimension

import dotterweide.Example
import dotterweide.editor.ColorScheme
import dotterweide.ide.MainFrame

import scala.swing.event.WindowClosed
import scala.swing.{Swing, Window}

object Demo {
  sealed trait Laf
  object Laf {
    case object Default     extends Laf
    case object SubminLight extends Laf
    case object SubminDark  extends Laf
  }

  case class Config(stylingName: Option[String] = None,
                    structure: Boolean = false, flash: Boolean = false, laf: Laf = Laf.Default)

  def main(args: Array[String]): Unit = {
    val default = Config()

    val p = new scopt.OptionParser[Config]("Demo") {
      opt[String]('c', "colors")
        .text(s"Select color scheme name (one of ${ColorScheme.names.mkString(", ")})")
        .validate { v => if (ColorScheme.names.contains(v.capitalize)) success else failure(s"Unknown scheme $v") }
        .action { (v, c) => c.copy(stylingName = Some(v.capitalize)) }

      opt[Unit]("structure")
        .text("Show structure view")
        .action { (_, c) => c.copy(structure = true) }

      opt[Unit]("flash")
        .text("Demo flash function via shift-return")
        .action { (_, c) => c.copy(flash = true) }

      opt[Unit]("submin-light")
        .text("Use Submin light look-and-feel")
        .action { (_, c) => c.copy(laf = Laf.SubminLight) }

      opt[Unit]("submin-dark")
        .text("Use Submin dark look-and-feel")
        .action { (_, c) => c.copy(laf = Laf.SubminDark) }
    }
    p.parse(args, default).fold(sys.exit(1)) { config =>
//      config.laf match {
//        case Laf.SubminLight  => Submin.install(false)
//        case Laf.SubminDark   => Submin.install(true )
//        case Laf.Default      =>
//      }

      Swing.onEDT(run(config))
    }
  }

  def run(config: Config): Unit = {
    val example = Example(
      name      = "Hello World",
      mnemonic  = 'h',
      code      =
        """object HelloWorld {
          |  def main(args: Array[String]): Unit =
          |    println("Hello World!")
          |}
          |""".stripMargin
    )
    val language  = new ScalaLanguage(examples = example :: Nil)

    val code  = example.code
    val frame = new MainFrame(language, code, stylingName = config.stylingName, structure = config.structure,
      flash = config.flash)
    frame.preferredSize = new Dimension(874, 696)
    open(frame)
    frame.listenTo(frame)
    frame.reactions += {
      case WindowClosed(_) => sys.exit()
    }
  }

  private def open(window: Window): Unit = {
    window.pack()
    window.centerOnScreen()
    window.open()
  }
}
