package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ast.*
import java.util.*

class CFGBuilder {
    companion object {
        const val PYTHON_MAIN_IF = "__name__==\"__main__\""
    }

    private val cfgsInConstruction = Stack<ControlFlowGraph>()

    private var cfg: ControlFlowGraph = emptyCfg()

    fun makeCFG(statements: Statements): ControlFlowGraph {
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
        return cfg.also { cfg = cfgsInConstruction.pop() }
    }

    private fun handleReturnStatement(statement: ReturnStatement) =
        Node(NodeType.END, "${statement.returnVariation} ${statement.returnValue}")
                .also { TODO("cfg[it] = emptySet()") }

    private fun handleBreakStatement(statement: BreakStatement) =
            Node(NodeType.FLOW, "break").also { TODO("cfg[it] = emptySet()") }

    private fun handleSimpleStatement(statement: SimpleStatement) =
            Node(NodeType.FLOW, statement.text).also { cfg = it.connectTo(cfg) }

    private fun handleIfStatement(statement: IfStatement) {
        var ifStatementCfg = makeCFG(statement.elseBranch)
        for ((test, trueBranch) in (statement.testToBranch.map { it.key to it.value }.asReversed())) {
            ifStatementCfg = Node(NodeType.CONDITION, test)
                    .join(makeCFG(trueBranch), ifStatementCfg, "yes", "no")
        }
        cfg = ifStatementCfg.concat(cfg)
    }

    private fun handleContinueStatement(statement: ContinueStatement): Node {
        TODO("not implemented")
    }

    private fun handleLoopStatement(statement: LoopStatement) {
        val loopHead = Node(NodeType.LOOP_BEGIN, statement.condition)
        val loopBody = makeCFG(statement.body)
        var loopCfg = if (statement.elseBranch.isNotEmpty()) {
            loopBody.findBreakEnds().forEach { cfg = it.connectTo(cfg, phantom = true) }
            loopHead.join(loopBody, makeCFG(statement.elseBranch).concat(cfg), "yes", "no")
        } else {
            loopHead.join(loopBody, cfg, "yes", "no")
        }
        loopBody.findNonBreakEnds().forEach { loopCfg = it.connectTo(loopCfg, phantom = true) }
        cfg = loopCfg
    }

    private fun removeMainIf(statements: Statements): Statements {
        val (mainIfStatement, mainIfIndex) = statements.asSequence()
                .filter { it is IfStatement }
                .mapIndexed { index, statement -> statement as IfStatement to index }
                .find { it.first.testToBranch.containsKey(PYTHON_MAIN_IF) }
                ?: return statements
        val mainStatements = mainIfStatement.testToBranch[PYTHON_MAIN_IF]!!
        val statementsBeforeMainIf = statements.subList(0, mainIfIndex)
        val statementsAfterMainIf = statements.subList(mainIfIndex + 1, statements.size)
        return statementsBeforeMainIf + mainStatements + statementsAfterMainIf
    }
}