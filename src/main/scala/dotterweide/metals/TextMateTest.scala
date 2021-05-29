package dotterweide.metals

import org.eclipse.tm4e.core.registry.Registry

object TextMateTest {
  def main(args: Array[String]): Unit =
    run()

  def run(): Unit = {
    val registry    = new Registry()
//    val grammar     = registry.loadGrammarFromPathSync("JavaScript.tmLanguage",
//      TextMateTest.getClass.getResourceAsStream("/JavaScript.tmLanguage"))

//    val grammar     = registry.loadGrammarFromPathSync("JavaScript.tmLanguage.json",
//      TextMateTest.getClass.getResourceAsStream("/JavaScript.tmLanguage.json"))

    val grammar     = registry.loadGrammarFromPathSync("Scala.tmLanguage.json",
      TextMateTest.getClass.getResourceAsStream("/Scala.tmLanguage.json"))

    val source      = "object Foo { def main(args: Array[String]): Unit = println(\"Hello\") }"

    val lineTokens  = grammar.tokenizeLine(source)
    val tokens      = lineTokens.getTokens
    tokens.foreach { tk =>
      val sub = source.substring(tk.getStartIndex, tk.getEndIndex)
    	println(s"[${tk.getStartIndex} to ${tk.getEndIndex}] source '$sub' has scopes ${tk.getScopes}")
    }
  }
}
