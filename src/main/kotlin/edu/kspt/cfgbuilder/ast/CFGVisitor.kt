package edu.kspt.cfgbuilder.ast

import edu.kspt.cfgbuilder.Python3BaseVisitor
import edu.kspt.cfgbuilder.Python3Parser

class CFGVisitor : Python3BaseVisitor<Statements>() {
    companion object {
        private const val IN = "in"

        private val RETURNISH_TOKENS = sequenceOf("return", "yield")

        private val SEPARABLE_TOKENS = sequenceOf("from", "import", "global", "nonlocal",
                "assert", "del", "raise")
    }

    override fun defaultResult() = emptyList<Statement>()

    override fun aggregateResult(result: Statements, nextResult: Statements) = result + nextResult

    override fun visitSmall_stmt(statement: Python3Parser.Small_stmtContext): Statements {
        val statementText = statement.text
        return if (statement.isReturnStatement() || statement.isYieldStatement()) {
            listOf(createAppropriateReturnStatement(statementText))
        } else if (statement.isBreakStatement()) {
            listOf(BreakStatement())
        } else if (statement.isContinueStatement()) {
            listOf(ContinueStatement())
        } else {
            listOf(SimpleStatement(maintainSpacesInText(statementText)))
        }
    }

    private fun createAppropriateReturnStatement(statementText: String) = RETURNISH_TOKENS
            .filter { statementText.startsWith(it) }
            .map { ReturnStatement(it, statementText.substring(it.length)) }
            .single()

    private fun maintainSpacesInText(statementText: String) = SEPARABLE_TOKENS
            .filter { statementText.startsWith(it) }
            .map { "$it ${statementText.drop(it.length)}" }
            .singleOrNull() ?: statementText

    override fun visitIf_stmt(ifStatement: Python3Parser.If_stmtContext): Statements {
        val conditionToBranch = LinkedHashMap<String, Statements>()
        (0 until ifStatement.test().size).associateTo(conditionToBranch) { i ->
            ifStatement.test(i).text to visitChildren(ifStatement.suite(i))
        }
        val elseBranch = if (ifStatement.test().size != ifStatement.suite().size) {
            visitChildren(ifStatement.suite().last())
        } else emptyList()
        return listOf(IfStatement(conditionToBranch, elseBranch))
    }

    override fun visitFor_stmt(forStatement: Python3Parser.For_stmtContext): Statements {
        val condition = "${forStatement.exprlist().text} $IN ${forStatement.testlist().text}"
        return listOf(createLoopStatement(condition, forStatement.suite()))
    }

    override fun visitWhile_stmt(whileStatement: Python3Parser.While_stmtContext): Statements {
        val condition = whileStatement.test().text
        return listOf(createLoopStatement(condition, whileStatement.suite()))
    }

    private fun createLoopStatement(condition: String, suite: List<Python3Parser.SuiteContext>) =
            if (suite.size == 2) {
                LoopStatement(condition, visitChildren(suite[0]), visitChildren(suite[1]))
            } else {
                LoopStatement(condition, visitChildren(suite[0]))
            }

    override fun visitFuncdef(ctx: Python3Parser.FuncdefContext) = emptyList<Statement>()
}