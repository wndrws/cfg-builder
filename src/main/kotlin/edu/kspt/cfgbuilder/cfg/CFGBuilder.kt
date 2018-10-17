package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ast.*
import mu.KLogging
import java.util.*

class CFGBuilder(private val encloseOnlyIfNeeded: Boolean = true) {
    companion object : KLogging() {
        const val PYTHON_MAIN_IF = "__name__==\"__main__\""
    }

    private val cfgsInConstruction = Stack<ControlFlowGraph>()

    private var loopsDepth = 0

    private val hangingLinks = mutableListOf<Pair<Node, String>>()

    private var cfg: ControlFlowGraph = emptyCfg()

    fun makeCFG(statements: Statements, funName: String = ""): ControlFlowGraph {
        cfgsInConstruction.push(cfg).also { cfg = emptyCfg() }
        val statementsReversedView = removeMainIf(statements).asReversed()
        for (statement in statementsReversedView) {
            when (statement) {
                is ReturnStatement -> handleReturnStatement(statement)
                is IfStatement -> handleIfStatement(statement)
                is LoopStatement -> handleLoopStatement(statement)
                is SimpleStatement -> handleSimpleStatement(statement)
                is BreakStatement -> handleBreakStatement(statement)
                is ContinueStatement -> handleContinueStatement(statement)
                else -> throw IllegalStateException("Unknown statement type")
            }
        }
        if (cfgsInConstruction.size == 1) enclose(funName)
        return cfg.also { cfg = cfgsInConstruction.pop() }
    }

    private fun handleReturnStatement(statement: ReturnStatement) = startCfgBuilding(
            Node(NodeType.END, "${statement.returnVariation} ${statement.returnValue}"), statement)

    private fun handleBreakStatement(statement: BreakStatement) =
            if (loopsDepth > 0) {
                startCfgBuilding(Node(NodeType.BREAK, "break"), statement)
            } else throw IllegalStateException("Input is incorrect: 'break' not inside loop")

    private fun handleContinueStatement(statement: ContinueStatement) =
            if (loopsDepth > 0) {
                val node = Node(NodeType.CONTINUE, "continue")
                startCfgBuilding(node, statement)
                hangingLinks.add(node to "")
            } else throw IllegalStateException("Input is incorrect: 'continue' not inside loop")

    private fun startCfgBuilding(node: Node, statement: Statement) {
        if (cfg.isNotEmpty()) {
            logger.warn { "Unreachable code after $statement (node $node) will not be placed in CFG" }
        }
        cfg = node.connectTo(emptyCfg())
    }

    private fun handleSimpleStatement(statement: SimpleStatement) =
            Node(NodeType.FLOW, statement.text).also { cfg = it.connectTo(cfg) }

    private fun handleIfStatement(statement: IfStatement) {
        var ifStatementCfg = makeCFG(statement.elseBranch)
        val testsToBranches = statement.testToBranch.map { it.key to it.value }
        for ((test, trueBranch) in testsToBranches.asReversed()) {
            val condition = Node(NodeType.CONDITION, test)
            ifStatementCfg = if (ifStatementCfg.isNotEmpty()) {
                condition.join(makeCFG(trueBranch), ifStatementCfg, "yes", "no")
            } else if (cfg.isNotEmpty()) {
                condition.join(makeCFG(trueBranch), cfg.findStart(), "yes", "no")
            } else {
                condition.connectTo(makeCFG(trueBranch), "yes")
                        .also { hangingLinks.add(condition to "no") }
            }
        }
        cfg = if (cfg.isEmpty()) ifStatementCfg else ifStatementCfg.concat(cfg)
    }

    private fun handleLoopStatement(statement: LoopStatement) {
        loopsDepth++
        val loopHead = Node(NodeType.LOOP_BEGIN, statement.condition)
        val loopBody = makeCFG(statement.body)
        var loopCfg = if (statement.elseBranch.isNotEmpty()) {
            loopBody.findBreakEnds().forEach { cfg = it.connectTo(cfg, phantom = true) }
            loopHead.join(loopBody, makeCFG(statement.elseBranch).concat(cfg), "yes", "no")
        } else {
            loopHead.join(loopBody, cfg, "yes", "no")
        }
        loopBody.findNonBreakEnds().forEach { loopCfg = it.connectTo(loopCfg, phantom = true) }
        hangingLinks.forEach { (node, linkText) ->
            loopCfg = node.connectTo(loopCfg, linkText, phantom = true)
        }.also { hangingLinks.clear() }
        cfg = loopCfg
        loopsDepth--
    }

    private fun removeMainIf(statements: Statements): Statements {
        val (mainIfStatement, mainIfIndex) = statements.asSequence()
                .filter { it is IfStatement }
                .mapIndexed { index, statement -> statement as IfStatement to index }
                .find { it.first.testToBranch.containsKey(PYTHON_MAIN_IF) }
                ?: return statements
        logger.info { "Standard main if-statement condition will not be displayed" }
        val mainStatements = mainIfStatement.testToBranch[PYTHON_MAIN_IF]!!
        val statementsBeforeMainIf = statements.subList(0, mainIfIndex)
        val statementsAfterMainIf = statements.subList(mainIfIndex + 1, statements.size)
        return statementsBeforeMainIf + mainStatements + statementsAfterMainIf
    }

    private fun enclose(funName: String) {
        if (encloseOnlyIfNeeded && hangingLinks.isEmpty()) return
        val lastNode = Node(NodeType.END, "return")
        cfg = Node(NodeType.BEGIN, funName).connectTo(cfg.appendNode(lastNode))
        hangingLinks.forEach { (node, linkText) ->
            cfg = cfg.addLinkDirectly(node, LinkTo(lastNode, linkText))
        }.also { hangingLinks.clear() }
    }
}