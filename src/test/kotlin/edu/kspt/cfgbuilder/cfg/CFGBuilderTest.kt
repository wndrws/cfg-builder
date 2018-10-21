package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ParserFacade
import edu.kspt.cfgbuilder.ast.CFGVisitor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
        // given
        val pythonCode = getPythonCodeExample(name)
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val cfg = CFGBuilder(true).makeCFG(stmts)
        // then
        cfg.prettyPrint()
        assertThat(cfg).hasSameSizeAs(expectedCfg)
        assertThat(cfg).containsAllEntriesOf(expectedCfg)
    }

    @Suppress("unused")
    fun testMakeCFG() = listOf(
            "if_statement" to `CFG with simple if`(),
            "if_else_statement" to `CFG with if-else`(),
            "if_elif_statement" to `CFG with if-elif`(),
            "if_elif_else_statement" to `CFG with if-elif-else`(),
            "for_statement" to `CFG with simple for`(),
            "for_else_statement" to `CFG with for-else without break`(),
            "for_else_statement_plain_break" to `CFG with for-else with plain break`(),
            "for_else_statement_nested_break" to `CFG with for-else with nested break`(),
            "for_else_statement_plain_continue" to `CFG with for-else with plain continue`(),
            "for_else_statement_nested_continue" to `CFG with for-else with nested continue`(),
            "nested_if_statements" to `CFG with nested ifs`(),
            "nested_if_else_statements" to `CFG with nested if-elses`(),
            "nested_for_statements" to `CFG with nested fors`(),
            "nested_for_statements_with_break" to `CFG with nested fors with break`(),
            "nested_for_statements_with_break_alt" to `CFG with nested fors with break alt`(),
            "nested_for_statements_with_continue" to `CFG with nested fors with continue`(),
            "nested_for_statements_with_continue_alt" to `CFG with nested fors with continue alt`(),
            "complex_nested_statements" to `complex CFG with nested statements`(),
            "complex_nested_statements_alt" to `complex CFG with nested statements alt`()
    ).map { Arguments.of(it.first, it.second) }

    @Test
    fun `if 'continue' is not inside loop, exception is thrown`() {
        // given
        val pythonCode = "a = 2; continue; b = 3\n"
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val ex = catchThrowable { CFGBuilder().makeCFG(stmts) }
        // then
        assertThat(ex).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `if 'break' is not inside loop, exception is thrown`() {
        // given
        val pythonCode = "a = 2; break; b = 3\n"
        val stmts = CFGVisitor().visit(ParserFacade().parse(pythonCode))
        // when
        val ex = catchThrowable { CFGBuilder().makeCFG(stmts) }
        // then
        assertThat(ex).isInstanceOf(IllegalStateException::class.java)
    }

    private fun `CFG with simple if`(): ControlFlowGraph {
        val someValNode = Node(NodeType.FLOW, "someVal=123", 3)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 1)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 2)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        return mapOf(
                someValNode to setOf(LinkTo(ifCondition)),
                ifCondition to setOf(LinkTo(trueNode, "yes"), LinkTo(lastNode, "no")),
                trueNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with if-else`(): ControlFlowGraph {
        val someValNode = Node(NodeType.FLOW, "someVal=123", 4)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 2)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 3)
        val elseNode = Node(NodeType.FLOW, "print(\"0\")", 1)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        return mapOf(
                someValNode to setOf(LinkTo(ifCondition)),
                ifCondition to setOf(LinkTo(trueNode, "yes"), LinkTo(elseNode, "no")),
                elseNode to setOf(LinkTo(lastNode)),
                trueNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with if-elif`(): ControlFlowGraph {
        val someValNode = Node(NodeType.FLOW, "someVal=123", 6)
        val ifCondition = Node(NodeType.CONDITION, "someVal>1", 4)
        val elifCondition = Node(NodeType.CONDITION, "someVal<1", 1)
        val trueNode = Node(NodeType.FLOW, "print(\">1\")", 5)
        val elifNode1 = Node(NodeType.FLOW, "print(\"<1\")",3)
        val elifNode2 = Node(NodeType.FLOW, "print(\"<2\")",2)
        val lastNode = Node(NodeType.FLOW, "smth=\"\"", 0)
        return mapOf(
                someValNode to setOf(LinkTo(ifCondition)),
                ifCondition to setOf(LinkTo(trueNode, "yes"), LinkTo(elifCondition, "no")),
                elifCondition to setOf(LinkTo(elifNode1, "yes"), LinkTo(lastNode, "no")),
                trueNode to setOf(LinkTo(lastNode)),
                elifNode1 to setOf(LinkTo(elifNode2)),
                elifNode2 to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

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

    private fun `CFG with for-else with plain continue`(): ControlFlowGraph {
        val firstNode = Node(NodeType.FLOW, "smth1=1", 8)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 7)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 5)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 4)
        val continueNode = Node(NodeType.CONTINUE, "continue", 3)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 6)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        return mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(continueNode)),
                continueNode to setOf(LinkTo(forNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with for-else with nested continue`(): ControlFlowGraph {
        val firstNode = Node(NodeType.FLOW, "smth1=1", 9)
        val secondNode = Node(NodeType.FLOW, "smth2=2", 8)
        val forNode = Node(NodeType.LOOP_BEGIN, "i in range(smth2)", 1)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 6)
        val bodyNode2 = Node(NodeType.FLOW, "print(i+1)", 5)
        val ifNode = Node(NodeType.CONDITION, "i==3", 3)
        val continueNode = Node(NodeType.CONTINUE, "continue", 4)
        val innerElseNode = Node(NodeType.FLOW, "print(i+2)", 2)
        val elseNode = Node(NodeType.FLOW, "print(\"else\")", 7)
        val lastNode = Node(NodeType.FLOW, "smth3=3", 0)
        return mapOf(
                firstNode to setOf(LinkTo(secondNode)),
                secondNode to setOf(LinkTo(forNode)),
                forNode to setOf(LinkTo(bodyNode1, "yes"), LinkTo(elseNode, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(ifNode)),
                ifNode to setOf(LinkTo(continueNode, "yes"), LinkTo(innerElseNode, "no")),
                continueNode to setOf(LinkTo(forNode, phantom = true)),
                innerElseNode to setOf(LinkTo(forNode, phantom = true)),
                elseNode to setOf(LinkTo(lastNode)),
                lastNode to emptySet()
        )
    }

    private fun `CFG with nested ifs`(): ControlFlowGraph {
        val someValNode = Node(NodeType.FLOW, "someVal=123", 6)
        val outerIf = Node(NodeType.CONDITION, "someVal>1", 0)
        val print1 = Node(NodeType.FLOW, "print(\"1\")", 5)
        val middleIf = Node(NodeType.CONDITION, "someVal>2", 1)
        val print2 = Node(NodeType.FLOW, "print(\"2\")", 4)
        val innerIf = Node(NodeType.CONDITION, "someVal>3", 2)
        val print3 = Node(NodeType.FLOW, "print(\"3\")", 3)
        val endNode = Node(NodeType.END, "return", 7)
        val beginNode = Node(NodeType.BEGIN, "", 8)
        return mapOf(
                beginNode to setOf(LinkTo(someValNode)),
                someValNode to setOf(LinkTo(outerIf)),
                outerIf to setOf(LinkTo(print1, "yes"), LinkTo(endNode, "no")),
                print1 to setOf(LinkTo(middleIf)),
                middleIf to setOf(LinkTo(print2, "yes"), LinkTo(endNode, "no")),
                print2 to setOf(LinkTo(innerIf)),
                innerIf to setOf(LinkTo(print3, "yes"), LinkTo(endNode, "no")),
                print3 to setOf(LinkTo(endNode)),
                endNode to emptySet()
        )
    }

    private fun `CFG with nested if-elses`(): ControlFlowGraph {
        val someValNode = Node(NodeType.FLOW, "someVal=123", 8)
        val outerIf = Node(NodeType.CONDITION, "someVal>1", 1)
        val print1 = Node(NodeType.FLOW, "print(\"1\")", 7)
        val else1 = Node(NodeType.FLOW, "print(\"not 1\")", 0)
        val middleIf = Node(NodeType.CONDITION, "someVal>2", 2)
        val print2 = Node(NodeType.FLOW, "print(\"2\")", 6)
        val innerIf = Node(NodeType.CONDITION, "someVal>3", 4)
        val print3 = Node(NodeType.FLOW, "print(\"3\")", 5)
        val else3 = Node(NodeType.FLOW, "print(\"not 3\")", 3)
        val endNode = Node(NodeType.END, "return", 9)
        val beginNode = Node(NodeType.BEGIN, "", 10)
        return mapOf(
                beginNode to setOf(LinkTo(someValNode)),
                someValNode to setOf(LinkTo(outerIf)),
                outerIf to setOf(LinkTo(print1, "yes"), LinkTo(else1, "no")),
                print1 to setOf(LinkTo(middleIf)),
                middleIf to setOf(LinkTo(print2, "yes"), LinkTo(endNode, "no")),
                print2 to setOf(LinkTo(innerIf)),
                innerIf to setOf(LinkTo(print3, "yes"), LinkTo(else3, "no")),
                print3 to setOf(LinkTo(endNode)),
                else1 to setOf(LinkTo(endNode)),
                else3 to setOf(LinkTo(endNode)),
                endNode to emptySet()
        )
    }

    private fun `CFG with nested fors`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 7)
        val outerFor = Node(NodeType.LOOP_BEGIN, "i in range(10)", 0)
        val middleFor = Node(NodeType.LOOP_BEGIN, "j in range(20)", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "k in range(3)", 2)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 5)
        val bodyNode2 = Node(NodeType.FLOW, "print(j)", 4)
        val bodyNode3 = Node(NodeType.FLOW, "print(k)", 3)
        val endNode = Node(NodeType.END, "return", 6)
        return mapOf(
                beginNode to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(middleFor, "yes"), LinkTo(endNode, "no")),
                middleFor to setOf(LinkTo(bodyNode1, "yes"), LinkTo(outerFor, "no", phantom = true)),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(innerFor)),
                innerFor to setOf(LinkTo(bodyNode3, "yes"), LinkTo(middleFor, "no", phantom = true)),
                bodyNode3 to setOf(LinkTo(innerFor, phantom = true)),
                endNode to emptySet()
        )
    }

    private fun `CFG with nested fors with break`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 9)
        val outerFor = Node(NodeType.LOOP_BEGIN, "i in range(10)", 0)
        val middleFor = Node(NodeType.LOOP_BEGIN, "j in range(20)", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "k in range(3)", 4)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 7)
        val bodyNode2 = Node(NodeType.FLOW, "print(j)", 6)
        val bodyNode3 = Node(NodeType.FLOW, "print(k)", 5)
        val ifNode = Node(NodeType.CONDITION, "i==10", 2)
        val breakNode = Node(NodeType.BREAK, "break", 3)
        val endNode = Node(NodeType.END, "return", 8)
        return mapOf(
                beginNode to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(middleFor, "yes"), LinkTo(endNode, "no")),
                middleFor to setOf(LinkTo(bodyNode1, "yes"), LinkTo(outerFor, "no", phantom = true)),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(innerFor)),
                innerFor to setOf(LinkTo(bodyNode3, "yes"), LinkTo(ifNode, "no")),
                bodyNode3 to setOf(LinkTo(innerFor, phantom = true)),
                ifNode to setOf(LinkTo(breakNode, "yes"), LinkTo(middleFor, "no", phantom = true)),
                breakNode to setOf(LinkTo(outerFor, phantom = true)),
                endNode to emptySet()
        )
    }

    private fun `CFG with nested fors with break alt`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 10)
        val outerFor = Node(NodeType.LOOP_BEGIN, "i in range(10)", 0)
        val middleFor = Node(NodeType.LOOP_BEGIN, "j in range(20)", 2)
        val innerFor = Node(NodeType.LOOP_BEGIN, "k in range(3)", 5)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 8)
        val bodyNode2 = Node(NodeType.FLOW, "print(j)", 7)
        val bodyNode3 = Node(NodeType.FLOW, "print(k)", 6)
        val ifNode = Node(NodeType.CONDITION, "i==10", 3)
        val breakNode = Node(NodeType.BREAK, "break", 4)
        val nodeAfterBreak = Node(NodeType.FLOW, "print(\"broken?\")", 1)
        val endNode = Node(NodeType.END, "return", 9)
        return mapOf(
                beginNode to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(middleFor, "yes"), LinkTo(endNode, "no")),
                middleFor to setOf(LinkTo(bodyNode1, "yes"), LinkTo(nodeAfterBreak, "no")),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(innerFor)),
                innerFor to setOf(LinkTo(bodyNode3, "yes"), LinkTo(ifNode, "no")),
                bodyNode3 to setOf(LinkTo(innerFor, phantom = true)),
                ifNode to setOf(LinkTo(breakNode, "yes"), LinkTo(middleFor, "no", phantom = true)),
                breakNode to setOf(LinkTo(nodeAfterBreak, phantom = true)),
                nodeAfterBreak to setOf(LinkTo(outerFor, phantom = true)),
                endNode to emptySet()
        )
    }

    private fun `CFG with nested fors with continue`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 9)
        val outerFor = Node(NodeType.LOOP_BEGIN, "i in range(10)", 0)
        val middleFor = Node(NodeType.LOOP_BEGIN, "j in range(20)", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "k in range(3)", 2)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 7)
        val bodyNode2 = Node(NodeType.FLOW, "print(j)", 6)
        val bodyNode3 = Node(NodeType.FLOW, "print(k)", 3)
        val ifNode = Node(NodeType.CONDITION, "i==10", 4)
        val continueNode = Node(NodeType.CONTINUE, "continue", 5)
        val endNode = Node(NodeType.END, "return", 8)
        return mapOf(
                beginNode to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(middleFor, "yes"), LinkTo(endNode, "no")),
                middleFor to setOf(LinkTo(bodyNode1, "yes"), LinkTo(outerFor, "no", phantom = true)),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(ifNode)),
                ifNode to setOf(LinkTo(continueNode, "yes"), LinkTo(innerFor, "no")),
                innerFor to setOf(LinkTo(bodyNode3, "yes"), LinkTo(middleFor, "no", phantom = true)),
                bodyNode3 to setOf(LinkTo(innerFor, phantom = true)),
                continueNode to setOf(LinkTo(middleFor, phantom = true)),
                endNode to emptySet()
        )
    }

    private fun `CFG with nested fors with continue alt`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 10)
        val outerFor = Node(NodeType.LOOP_BEGIN, "i in range(10)", 0)
        val middleFor = Node(NodeType.LOOP_BEGIN, "j in range(20)", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "k in range(3)", 3)
        val ifNode = Node(NodeType.CONDITION, "i==10", 7)
        val continueNode1 = Node(NodeType.CONTINUE, "continue", 8)
        val bodyNode1 = Node(NodeType.FLOW, "print(i)", 6)
        val bodyNode2 = Node(NodeType.FLOW, "print(j)", 5)
        val bodyNode3 = Node(NodeType.FLOW, "print(k)", 4)
        val continueNode2 = Node(NodeType.CONTINUE, "continue", 2)
        val endNode = Node(NodeType.END, "return", 9)
        return mapOf(
                beginNode to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(middleFor, "yes"), LinkTo(endNode, "no")),
                middleFor to setOf(LinkTo(ifNode, "yes"), LinkTo(outerFor, "no", phantom = true)),
                ifNode to setOf(LinkTo(continueNode1, "yes"), LinkTo(bodyNode1, "no")),
                continueNode1 to setOf(LinkTo(middleFor, phantom = true)),
                bodyNode1 to setOf(LinkTo(bodyNode2)),
                bodyNode2 to setOf(LinkTo(innerFor)),
                innerFor to setOf(LinkTo(bodyNode3, "yes"), LinkTo(continueNode2, "no")),
                bodyNode3 to setOf(LinkTo(innerFor, phantom = true)),
                continueNode2 to setOf(LinkTo(middleFor, phantom = true)),
                endNode to emptySet()
        )
    }

    private fun `complex CFG with nested statements`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 10)
        val node1 = Node(NodeType.FLOW, "lower=900", 8)
        val node2 = Node(NodeType.FLOW, "upper=1000", 7)
        val node3 = Node(NodeType.FLOW, "print(\"Prime numbers between\",lower,\"and\",upper,\"are:\")", 6)
        val outerFor = Node(NodeType.LOOP_BEGIN, "num in range(lower,upper+1)", 0)
        val outerIf = Node(NodeType.CONDITION, "num>1", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "i in range(2,num)", 2)
        val innerIf = Node(NodeType.CONDITION, "(num%i)==0", 3)
        val breakNode = Node(NodeType.BREAK, "break", 4)
        val elseNode = Node(NodeType.FLOW, "print(num)", 5)
        val endNode = Node(NodeType.END, "return", 9)
        return mapOf(
                beginNode to setOf(LinkTo(node1)),
                node1 to setOf(LinkTo(node2)),
                node2 to setOf(LinkTo(node3)),
                node3 to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(outerIf, "yes"), LinkTo(endNode, "no")),
                outerIf to setOf(LinkTo(innerFor, "yes"), LinkTo(outerFor, "no", phantom = true)),
                innerFor to setOf(LinkTo(innerIf, "yes"), LinkTo(elseNode, "no")),
                innerIf to setOf(LinkTo(breakNode, "yes"), LinkTo(innerFor, "no", phantom = true)),
                breakNode to setOf(LinkTo(outerFor, phantom = true)),
                elseNode to setOf(LinkTo(outerFor, phantom = true)),
                endNode to emptySet()
        )
    }

    private fun `complex CFG with nested statements alt`(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "", 11)
        val node1 = Node(NodeType.FLOW, "lower=900", 9)
        val node2 = Node(NodeType.FLOW, "upper=1000", 8)
        val node3 = Node(NodeType.FLOW, "print(\"Prime numbers between\",lower,\"and\",upper,\"are:\")", 7)
        val outerFor = Node(NodeType.LOOP_BEGIN, "num in range(lower,upper+1)", 0)
        val outerIf = Node(NodeType.CONDITION, "num>1", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "i in range(2,num)", 3)
        val innerIf = Node(NodeType.CONDITION, "(num%i)==0", 4)
        val breakNode = Node(NodeType.BREAK, "break", 5)
        val elseNode = Node(NodeType.FLOW, "print(num)", 6)
        val nodeAfterForElse = Node(NodeType.FLOW, "print(\"done!\")", 2)
        val endNode = Node(NodeType.END, "return", 10)
        return mapOf(
                beginNode to setOf(LinkTo(node1)),
                node1 to setOf(LinkTo(node2)),
                node2 to setOf(LinkTo(node3)),
                node3 to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(outerIf, "yes"), LinkTo(endNode, "no")),
                outerIf to setOf(LinkTo(innerFor, "yes"), LinkTo(outerFor, "no", phantom = true)),
                innerFor to setOf(LinkTo(innerIf, "yes"), LinkTo(elseNode, "no")),
                innerIf to setOf(LinkTo(breakNode, "yes"), LinkTo(innerFor, "no", phantom = true)),
                breakNode to setOf(LinkTo(nodeAfterForElse, phantom = true)),
                elseNode to setOf(LinkTo(nodeAfterForElse)),
                nodeAfterForElse to setOf(LinkTo(outerFor, phantom = true)),
                endNode to emptySet()
        )
    }
}