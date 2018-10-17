package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ast.*
import mu.KLogging
import java.util.*

class CFGBuilder(private val encloseOnlyIfNeeded: Boolean = true) {
    companion object : KLogging() {
        const val PYTHON_MAIN_IF = "__name__==\"__main__\""
    }

    private val cfgsInConstruction = Stack<ControlFlowGraph>()

    private var cfg: ControlFlowGraph = emptyCfg()

    private var loopsDepth = 0

    private var depth = -1

    private val hangingLinksByDepth = mutableListOf<MutableList<Pair<Node, String>>>()

    fun makeCFG(statements: Statements, funName: String = ""): ControlFlowGraph {
        cfgsInConstruction.push(cfg).also { cfg = emptyCfg() }
        depth++
        val statementsReversedView = removeMainIf(statements).asReversed()
        for (statement in statementsReversedView) {
            when (statement) {
                is ReturnStatement -> handleReturnStatement(statement)
                is IfStatement -> handleIfStatement(statement)
                is LoopStatement -> { loopsDepth++; handleLoopStatement(statement); loopsDepth-- }
                is SimpleStatement -> handleSimpleStatement(statement)
                is BreakStatement -> handleBreakStatement(statement)
                is ContinueStatement -> handleContinueStatement(statement)
                else -> throw IllegalStateException("Unknown statement type")
            }
        }
        depth--
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
                addHangingLink(node to "")
            } else throw IllegalStateException("Input is incorrect: 'continue' not inside loop")

    private fun startCfgBuilding(node: Node, statement: Statement) {
        if (cfg.isNotEmpty()) {
            logger.warn { "Unreachable code after $statement (node $node) will not be placed in CFG" }
        }
        cfg = node.connectTo(emptyCfg())
    }

    private fun addHangingLink(hang: Pair<Node, String>) {
        while (depth >= hangingLinksByDepth.size) {
            hangingLinksByDepth.add(mutableListOf())
        }
        hangingLinksByDepth[depth].add(hang)
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
                condition.join(makeCFG(trueBranch), cfg, "yes", "no")
            } else {
                condition.connectTo(makeCFG(trueBranch), "yes")
                        .also { addHangingLink(condition to "no") }
            }
        }
        cfg = if (cfg.isEmpty()) ifStatementCfg else ifStatementCfg.concat(cfg)
    }

    private fun handleLoopStatement(statement: LoopStatement) {
        val loopHead = Node(NodeType.LOOP_BEGIN, statement.condition)
        val loopBody = makeCFG(statement.body)
        val loopCfg = when {
            statement.elseBranch.isNotEmpty() -> makeLoopCfgWithElse(loopHead, loopBody, statement)
            cfg.isNotEmpty() -> makeSimpleLoopCfg(loopHead, loopBody)
            else -> makeLoopCfgWithHangingExit(loopHead, loopBody)
        }
        cfg = closeHangingLinks(closeLoop(loopBody, loopCfg))
    }

    private fun makeLoopCfgWithElse(loopHead: Node, loopBody: ControlFlowGraph,
                                    statement: LoopStatement): ControlFlowGraph {
        loopBody.findBreakEnds().forEach { cfg = it.connectTo(cfg, phantom = true) }
        return loopHead.join(loopBody, makeCFG(statement.elseBranch).concat(cfg), "yes", "no")
    }

    private fun makeSimpleLoopCfg(loopHead: Node, loopBody: ControlFlowGraph): ControlFlowGraph {
        loopBody.findBreakEnds().forEach { cfg = it.connectTo(cfg, phantom = true) }
        return loopHead.join(loopBody, cfg, "yes", "no")
    }

    private fun makeLoopCfgWithHangingExit(loopHead: Node, loopBody: ControlFlowGraph): ControlFlowGraph {
        return loopHead.connectTo(loopBody, "yes").also { _ ->
            addHangingLink(loopHead to "no")
            loopBody.findBreakEnds().forEach { addHangingLink(it to "") }
        }
    }

    private fun closeLoop(loopBody: ControlFlowGraph, loopCfg: ControlFlowGraph): ControlFlowGraph {
        var updatedLoopCfg = loopCfg
        loopBody.findNonBreakEnds().forEach {
            updatedLoopCfg = it.connectTo(updatedLoopCfg, phantom = true)
        }
        return updatedLoopCfg
    }

    private fun closeHangingLinks(loopCfg: ControlFlowGraph): ControlFlowGraph {
        var updatedLoopCfg = loopCfg
        getHangingLinksToClose().forEach { (node, linkText) ->
            updatedLoopCfg = node.connectTo(updatedLoopCfg, linkText, phantom = true)
        }
        return updatedLoopCfg
    }

    private fun getHangingLinksToClose(): List<Pair<Node, String>> {
        return if (depth < hangingLinksByDepth.size) {
            hangingLinksByDepth.drop(depth + 1).flatMap { it }
                    .also { toClose -> hangingLinksByDepth.forEach { it.removeAll(toClose) } }
        } else {
            emptyList()
        }
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
        val hangingLinks = getHangingLinksToClose()
        if (encloseOnlyIfNeeded && hangingLinks.isEmpty()) return
        val lastNode = Node(NodeType.END, "return")
        cfg = Node(NodeType.BEGIN, funName).connectTo(cfg)
        hangingLinks.forEach { (node, linkText) ->
            cfg = cfg.linkNodesDirectly(node, LinkTo(lastNode, linkText))
        }
        cfg = cfg.appendNode(lastNode)
    }
}