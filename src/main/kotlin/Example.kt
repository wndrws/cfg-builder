import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import edu.kspt.cfgbuilder.ast.FunctionDefinitionListener
import java.io.File

object Example {
    @JvmStatic
    fun main(args: Array<String>) {
        val funcListener = FunctionDefinitionListener()
        val parserFacade = ParserFacade()
//        val astPrinter = AstPrinter()
//        astPrinter.print(parserFacade.parse(File("examples/simple.py")))
        val parseTree = parserFacade.parse(File("examples/simple.py"), funcListener)
        val stmts = CFGVisitor().visit(parseTree)
        println(stmts)
        println(funcListener.functions)

//        val node1 = Factory.node("If smth").with(Shape.DIAMOND)
//        val node2 = Factory.node("then").with(Shape.RECTANGLE)
//        val node3 = Factory.node("else").with(Shape.RECTANGLE)
//        val node4 = Factory.node("next").with(Shape.RECTANGLE)
//        val ifContent = { node0: Node ->
//            arrayOf(node0.link(node1),
//                    node1.link(
//                            Factory.to(node2).with(Label.of("yes")),
//                            Factory.to(node3).with(Label.of("no"))),
//                    node2.link(node4),
//                    node3.link(node4))
//        }
//        val graph = Factory.graph().directed().with(*ifContent(Factory.node("123")))
//        Graphviz.fromGraph(graph).render(Format.PNG).toFile(File("examples/ex1.png"))
    }
}