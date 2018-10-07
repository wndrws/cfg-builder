package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CFGBuilderTest {
    @Test
    fun `builder can handle code with if-statements`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithIfStatements()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithIfStatements(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("if_statement_example")
        val someValNode = Node(NodeType.FLOW, "someVal=123", 6)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 4)
        val elifCondition = Node(NodeType.CONDITION, "someVal<1", 2)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 5)
        val elifNode = Node(NodeType.FLOW, "print(\"<1\")",3)
        val elseNode = Node(NodeType.FLOW, "print(\"0\")", 1)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        val cfg: ControlFlowGraph = mutableMapOf(
                someValNode to setOf(LinkTo(ifCondition)),
                ifCondition to setOf(LinkTo(trueNode, "yes"), LinkTo(elifCondition, "no")),
                elifCondition to setOf(LinkTo(elifNode, "yes"), LinkTo(elseNode, "no")),
                elseNode to setOf(LinkTo(lastNode)),
                trueNode to setOf(LinkTo(lastNode)),
                elifNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
        return pythonCode to cfg
    }
}