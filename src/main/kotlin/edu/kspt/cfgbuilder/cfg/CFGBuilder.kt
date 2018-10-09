package edu.kspt.cfgbuilder.cfg

import edu.kspt.cfgbuilder.ast.*

class CFGBuilder {
    companion object {
        const val PYTHON_MAIN_IF = "__name__==\"__main__\""
    }

//    private val compoundStatementsExits = Stack<Node>()

    private var previousNode: Node? = null

    private var isInsideLoop = false

    private var cfg: ControlFlowGraph = mutableMapOf()

    fun makeCFG(statements: Statements): ControlFlowGraph {
        val statementsReversedView = removeMainIf(statements).asReversed()
        for (statement in statementsReversedView) {
            previousNode = when (statement) {
                is ReturnStatement -> handleReturnStatement(statement)
                is IfStatement -> handleIfStatement(statement)
                is LoopStatement -> handleLoopStatement(statement)
                is SimpleStatement -> handleSimpleStatement(statement)
                is BreakStatement -> handleBreakStatement(statement)
                is ContinueStatement -> handleContinueStatement(statement)
                else -> throw IllegalStateException("Unknown statement type")
            }
        }
        return cfg
    }

    private fun handleReturnStatement(statement: ReturnStatement): Node {
        val endNode = Node(NodeType.END, "${statement.returnVariation} ${statement.returnValue}")
        cfg[endNode] = emptySet()
        return endNode
    }

    private fun handleIfStatement(statement: IfStatement): Node {
//        compoundStatementsExits.push(previousNode ?: Node(NodeType.END, "return"))
        var ifStatementCfg = CFGBuilder().makeCFG(statement.elseBranch)
        for ((test, trueBranch) in (statement.testToBranch.map { it.key to it.value }.asReversed())) {
            ifStatementCfg = Node(NodeType.CONDITION, test)
                    .join(CFGBuilder().makeCFG(trueBranch), ifStatementCfg, "yes", "no")
        }
        previousNode?.let { ifStatementCfg.appendNode(it) }
//        compoundStatementsExits.pop()
        cfg.plusAssign(ifStatementCfg)
        return ifStatementCfg.findStart()
    }

    private fun handleContinueStatement(statement: ContinueStatement): Node {
        TODO("not implemented")
    }

    private fun handleBreakStatement(statement: BreakStatement): Node {
        TODO("not implemented")
    }

    private fun handleSimpleStatement(statement: SimpleStatement) =
            Node(NodeType.FLOW, statement.text).also { it.connectTo(cfg) }

    private fun handleLoopStatement(statement: LoopStatement): Node {
        if (statement.elseBranch.isEmpty()) {
            val loopHead = Node(NodeType.LOOP_BEGIN, statement.condition)
            val loopTail = Node(NodeType.LOOP_END, "")
            val loopStatement = CFGBuilder().makeCFG(statement.body)
            loopStatement.appendNode(loopTail)
            loopHead.connectTo(loopStatement)
            previousNode?.let {
                loopStatement.appendNode(it)
            }
            cfg.plusAssign(loopStatement)
            return loopStatement.findStart()
        } else {
            TODO("Not implemented")
        }
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