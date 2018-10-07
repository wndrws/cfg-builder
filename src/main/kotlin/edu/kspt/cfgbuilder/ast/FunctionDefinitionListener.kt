package edu.kspt.cfgbuilder.ast

import edu.kspt.cfgbuilder.Python3BaseListener
import edu.kspt.cfgbuilder.Python3Parser

class FunctionDefinitionListener : Python3BaseListener() {
    val functions = mutableListOf<Function>()

    private var currentClass: Python3Parser.ClassdefContext? = null

    override fun enterClassdef(ctx: Python3Parser.ClassdefContext) {
        currentClass = ctx
    }

    override fun exitClassdef(ctx: Python3Parser.ClassdefContext) {
        currentClass = null
    }

    override fun exitFuncdef(ctx: Python3Parser.FuncdefContext) {
        val className = currentClass?.NAME()?.text?.plus(".") ?: ""
        val definition = className + ctx.NAME().text + ctx.parameters().text
        functions += Function(definition, CFGVisitor().visit(ctx.suite()))
    }
}