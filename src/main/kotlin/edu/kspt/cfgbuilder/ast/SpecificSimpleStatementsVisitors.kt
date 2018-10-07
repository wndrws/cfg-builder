package edu.kspt.cfgbuilder.ast

import edu.kspt.cfgbuilder.Python3BaseVisitor
import edu.kspt.cfgbuilder.Python3Parser
import edu.kspt.cfgbuilder.Python3Parser.Small_stmtContext

abstract class IndicatingVisitor : Python3BaseVisitor<Boolean>() {
    override fun defaultResult() = false

    override fun aggregateResult(result: Boolean, nextResult: Boolean) = result || nextResult

    fun indicates(stmt: Small_stmtContext) = visit(stmt)!!
}

class BreakIndicatingVisitor : IndicatingVisitor() {
    override fun visitBreak_stmt(ctx: Python3Parser.Break_stmtContext) = true
}

class ContinueIndicatingVisitor : IndicatingVisitor() {
    override fun visitContinue_stmt(ctx: Python3Parser.Continue_stmtContext) = true
}

class ReturnIndicatingVisitor : IndicatingVisitor() {
    override fun visitReturn_stmt(ctx: Python3Parser.Return_stmtContext) = true
}

class YieldIndicatingVisitor : IndicatingVisitor() {
    override fun visitYield_stmt(ctx: Python3Parser.Yield_stmtContext) = true
}

fun Small_stmtContext.isBreakStatement() = BreakIndicatingVisitor().indicates(this)

fun Small_stmtContext.isContinueStatement() = ContinueIndicatingVisitor().indicates(this)

fun Small_stmtContext.isReturnStatement() = ReturnIndicatingVisitor().indicates(this)

fun Small_stmtContext.isYieldStatement() = YieldIndicatingVisitor().indicates(this)