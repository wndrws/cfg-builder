package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CFGBuilderTest {
    @BeforeEach
    fun setUp() {
        Node.CURRENT_MAX_ID = 0
    }

    @DisplayName("builds CFG for:")
    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun testMakeCFG(name: String, expectedCfg: ControlFlowGraph) {
        val pythonCode = getPythonCodeExample(name)
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    @Suppress("unused")
    fun testMakeCFG() = listOf(
            "if_elif_else_statement" to `CFG with if-elif-else`(),
            "for_statement" to `CFG with simple for`(),
            "for_else_statement" to `CFG with for-else without break`(),
            "for_else_statement_plain_break" to `CFG with for-else with plain break`(),
            "for_else_statement_nested_break" to `CFG with for-else with nested break`()
    ).map { Arguments.of(it.first, it.second) }

    private fun `CFG with if-elif-else`(): ControlFlowGraph {
        val someValNode = Node(NodeType.FLOW, "someVal=123", 7)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 5)
        val elifCondition = Node(NodeType.CONDITION, "someVal<1", 2)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 6)
        val elifNode1 = Node(NodeType.FLOW, "print(\"<1\")",4)
        val elifNode2 = Node(NodeType.FLOW, "print(\"<2\")",3)
        val elseNode = Node(NodeType.FLOW, "print(\"0\")", 1)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        return mapOf(
                someValNode to setOf(LinkTo(ifCondition)),
                ifCondition to setOf(LinkTo(trueNode, "yes"), LinkTo(elifCondition, "no")),
                elifCondition to setOf(LinkTo(elifNode1, "yes"), LinkTo(elseNode, "no")),
                elseNode to setOf(LinkTo(lastNode)),
                trueNode to setOf(LinkTo(lastNode)),
                elifNode1 to setOf(LinkTo(elifNode2)),
                elifNode2 to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with simple for`(): ControlFlowGraph {
        val firstNode = Node(NodeType.FLOW, "smth1=1", 5)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 4)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 3)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 2)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        return mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(lastNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(forNode, phantom = true)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with for-else without break`(): ControlFlowGraph {
        val firstNode = Node(NodeType.FLOW, "smth1=1", 6)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 5)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 3)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 2)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 4)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        return mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(forNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with for-else with plain break`(): ControlFlowGraph {
        val firstNode = Node(NodeType.FLOW, "smth1=1", 7)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 6)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 4)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 3)
        val breakNode = Node(NodeType.BREAK, "break", 2)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 5)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        return mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(breakNode)),
                breakNode to setOf(LinkTo(lastNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with for-else with nested break`(): ControlFlowGraph {
        val firstNode = Node(NodeType.FLOW, "smth1=1", 8)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 7)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 5)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 4)
        val ifNode = Node(NodeType.CONDITION, "i==2", 2)
        val breakNode = Node(NodeType.BREAK, "break", 3)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 6)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        return mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(ifNode)),
                ifNode to setOf(LinkTo(breakNode, "yes"), LinkTo(forNode, "no", phantom = true)),
                breakNode to setOf(LinkTo(lastNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }
}