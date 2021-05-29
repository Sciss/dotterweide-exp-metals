/*
 *  TextMateTest.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019-2021 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package dotterweide.metals

import org.eclipse.tm4e.core.registry.Registry

object TextMateTest {
  def main(args: Array[String]): Unit =
    run()

  def run(): Unit = {
//    val grammar     = registry.loadGrammarFromPathSync("JavaScript.tmLanguage",
//      TextMateTest.getClass.getResourceAsStream("/JavaScript.tmLanguage"))

//    val grammar     = registry.loadGrammarFromPathSync("JavaScript.tmLanguage.json",
//      TextMateTest.getClass.getResourceAsStream("/JavaScript.tmLanguage.json"))

    val registry    = new Registry()

    val grammar     = registry.loadGrammarFromPathSync("Scala.tmLanguage.json",
      TextMateTest.getClass.getResourceAsStream("/Scala.tmLanguage.json"))

    val source      = "object Foo { def main(args: Array[String]): Unit = println(\"Hello\") }"

    def mkTokens() = {
      val t1          = System.currentTimeMillis()
      val lineTokens  = grammar.tokenizeLine(source)
      val res         = lineTokens.getTokens
      val t2          = System.currentTimeMillis()
      println(s"Tokenization took ${t2-t1}ms.")
      res
    }
    val tokens = mkTokens()
    tokens.foreach { tk =>
      val sub = source.substring(tk.getStartIndex, tk.getEndIndex)
    	println(f"region ${tk.getStartIndex}% 3d until ${tk.getEndIndex}% 3d with text '$sub' has scopes ${tk.getScopes}")
    }
    println("Second run:")
    mkTokens()
  }
}
