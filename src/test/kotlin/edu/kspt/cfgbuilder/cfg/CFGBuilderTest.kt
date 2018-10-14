package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CFGBuilderTest {
    @Test
    fun `builder can handle code with if-elif-else statements`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithIfElifElseStatement()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithIfElifElseStatement(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("if_elif_else_statement_example")
        val someValNode = Node(NodeType.FLOW, "someVal=123", 7)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 5)
        val elifCondition = Node(NodeType.CONDITION, "someVal<1", 2)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 6)
        val elifNode1 = Node(NodeType.FLOW, "print(\"<1\")",4)
        val elifNode2 = Node(NodeType.FLOW, "print(\"<2\")",3)
        val elseNode = Node(NodeType.FLOW, "print(\"0\")", 1)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        val cfg: ControlFlowGraph = mapOf(
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
    fun `builder can handle code with for-statements without else-block`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithForStatement()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithForStatement(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("for_statement_example")
        val firstNode = Node(NodeType.FLOW, "smth1=1", 5)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 4)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 3)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 2)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        val cfg: ControlFlowGraph = mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(lastNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(forNode, phantom = true)),
                lastNode to emptySet()
        )
        return pythonCode to cfg
    }

    @Test
    fun `builder can handle code with for-statements with else-block without break`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithForElseStatement()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithForElseStatement(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("for_else_statement_example")
        val firstNode = Node(NodeType.FLOW, "smth1=1", 6)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 5)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 3)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 2)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 4)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        val cfg: ControlFlowGraph = mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(forNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
        return pythonCode to cfg
    }

    @Test
    fun `builder can handle code with for-statements with else-block and plain break`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithForElseStatementWithPlainBreak()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithForElseStatementWithPlainBreak(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("for_else_statement_plain_break_example")
        val firstNode = Node(NodeType.FLOW, "smth1=1", 7)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 6)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 4)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 3)
        val breakNode = Node(NodeType.BREAK, "break", 2)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 5)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        val cfg: ControlFlowGraph = mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(breakNode)),
                breakNode to setOf(LinkTo(lastNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
        return pythonCode to cfg
    }

    @Test
    fun `builder can handle code with for-statements with else-block and nested break`() {
        // given
        val (pythonCode, expectedCfg) = getPythonCodeWithForElseStatementWithNestedBreak()
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder().makeCFG(stmts)
        // then
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    private fun getPythonCodeWithForElseStatementWithNestedBreak(): Pair<String, ControlFlowGraph> {
        val pythonCode = getPythonCodeExample("for_else_statement_nested_break_example")
        val firstNode = Node(NodeType.FLOW, "smth1=1", 8)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 7)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 5)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 4)
        val ifNode = Node(NodeType.CONDITION, "i==2", 2)
        val breakNode = Node(NodeType.BREAK, "break", 3)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 6)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        val cfg: ControlFlowGraph = mapOf(
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
        return pythonCode to cfg
    }



    @BeforeEach
    fun setUp() {
        Node.CURRENT_MAX_ID = 0
    }
}