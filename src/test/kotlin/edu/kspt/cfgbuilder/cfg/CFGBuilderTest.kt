package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.KMutableProperty

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
        val someValNode = Node(NodeType.FLOW, "someVal=123", 7)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 5)
        val elifCondition = Node(NodeType.CONDITION, "someVal<1", 2)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 6)
        val elifNode1 = Node(NodeType.FLOW, "print(\"<1\")",4)
        val elifNode2 = Node(NodeType.FLOW, "print(\"<2\")",3)
        val elseNode = Node(NodeType.FLOW, "print(\"0\")", 1)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        val cfg: ControlFlowGraph = mutableMapOf(
                someValNode to setOf(LinkTo(ifCondition)),
                ifCondition to setOf(LinkTo(trueNode, "yes"), LinkTo(elifCondition, "no")),
                elifCondition to setOf(LinkTo(elifNode1, "yes"), LinkTo(elseNode, "no")),
                elseNode to setOf(LinkTo(lastNode)),
                trueNode to setOf(LinkTo(lastNode)),
                elifNode1 to setOf(LinkTo(elifNode2)),
                elifNode2 to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
        return pythonCode to cfg
    }

    @Test
    fun `builder can handle code with for-statements without else-blocks`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithForStatements()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithForStatements(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("for_statement_example")
        val firstNode = Node(NodeType.FLOW, "smth1=1", 6)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 5)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 4)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 3)
        val loopEndNode = Node(NodeType.LOOP_END, "", 2)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        val cfg: ControlFlowGraph = mutableMapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1)),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(loopEndNode)),
                loopEndNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
        return pythonCode to cfg
    }

    @BeforeEach
    fun setUp() {
        (Node.Companion::class.members.single { it.name == "CURRENT_MAX_ID" } as? KMutableProperty)
                ?.setter?.call(Node.Companion, 0)
    }
}