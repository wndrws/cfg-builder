import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import edu.kspt.cfgbuilder.ast.FunctionDefinitionListener
import edu.kspt.cfgbuilder.cfg.CFGBuilder
import edu.kspt.cfgbuilder.dot.asDOT
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val funcListener = FunctionDefinitionListener()
        val parserFacade = ParserFacade()
        val parseTree = parserFacade.parse(File("examples/simple.py"), funcListener)
        val stmts = CFGVisitor().visit(parseTree)
        val mainCfg = CFGBuilder().makeCFG(stmts, "main")
        val otherCfgs = funcListener.functions.map { CFGBuilder().makeCFG(it.body, it.definition) }
        render((otherCfgs + mainCfg).asDOT())
    }

    private fun render(dot: String) {
        Graphviz.fromGraph(Parser.read(dot))
                .height(1080)
                .render(Format.PNG)
                .toFile(File("examples/result.png"))
    }
}