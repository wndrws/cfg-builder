import edu.kspt.cfgbuilder.Python3Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext

class AstPrinter(private val ignoringWrappers: Boolean = true) {

    fun print(ctx: RuleContext) {
        explore(ctx, 0)
    }

    private fun explore(ctx: RuleContext, indentation: Int) {
        val toBeIgnored = (ignoringWrappers
                && ctx.childCount == 1
                && ctx.getChild(0) is ParserRuleContext)
        if (!toBeIgnored) {
            val ruleName = Python3Parser.ruleNames[ctx.ruleIndex]
            for (i in 0 until indentation) {
                print("  ")
            }
            println(ruleName + ": " + ctx.text)
        }
        for (i in 0 until ctx.childCount) {
            val element = ctx.getChild(i)
            if (element is RuleContext) {
                if (ctx is Python3Parser.Simple_stmtContext) {
                    return // No need to go deeper
                }
                explore(element, indentation + if (toBeIgnored) 0 else 1)
            }
        }
    }

}