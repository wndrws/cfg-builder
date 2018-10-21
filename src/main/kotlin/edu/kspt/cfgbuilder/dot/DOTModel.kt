package edu.kspt.cfgbuilder.dot

import edu.kspt.cfgbuilder.cfg.ControlFlowGraph
import edu.kspt.cfgbuilder.cfg.LinkTo
import edu.kspt.cfgbuilder.cfg.Node
import edu.kspt.cfgbuilder.cfg.NodeType

fun Node.nodeId() = "node" + this.id

fun Node.shape() = when(this.type) {
    NodeType.BEGIN -> "box"
    NodeType.FLOW -> "box"
    NodeType.CONDITION -> "diamond"
    NodeType.END -> "box"
    NodeType.LOOP_BEGIN -> "hexagon"
    NodeType.BREAK -> "box"
    NodeType.CONTINUE -> "box"
}

fun Node.style() = "solid,filled" + when(this.type) {
    NodeType.BEGIN -> ",rounded"
    NodeType.FLOW -> ""
    NodeType.CONDITION -> ""
    NodeType.END -> ",rounded"
    NodeType.LOOP_BEGIN -> ""
    NodeType.BREAK -> ""
    NodeType.CONTINUE -> ""
}

fun Node.escapedText() = this.text.replace(""""""", """\"""")

fun LinkTo.escapedText() = this.text.replace(""""""", """\"""")

fun ControlFlowGraph.asDOT(name: String) = StringBuilder()
        .startGraph(name)
        .describeCommonStyle()
        .declareNodes(this.keys)
        .describeCfg(this)
        .endGraph()
        .toString()

private fun StringBuilder.startGraph(name: String): StringBuilder {
    appendln("digraph $name {")
    return this
}

private fun StringBuilder.describeCommonStyle(): StringBuilder {
//    indent().appendln("splines=ortho")
    indent().appendln("""node [fillcolor="#eeeeee" penwidth=0.75]""")
    appendln()
    return this
}


private fun StringBuilder.declareNodes(nodes: Set<Node>): StringBuilder {
    nodes.forEach {
        indent()
        append("${it.nodeId()} [")
        append("label=\"${it.escapedText()}\", ")
        append("shape=\"${it.shape()}\", ")
        append("style=\"${it.style()}\"")
        appendln("];")
    }
    appendln()
    return this
}

private fun StringBuilder.indent(): StringBuilder {
    append("    ")
    return this
}

private fun StringBuilder.describeCfg(cfg: ControlFlowGraph): StringBuilder {
    cfg.forEach { node, links ->
        links.forEach {
            indent()
            append("${node.nodeId()} -> ${it.otherNode.nodeId()} ")
            appendln("""[label="${it.escapedText()}"];""")
        }
    }
    return this
}

private fun StringBuilder.endGraph(): StringBuilder {
    appendln("}")
    return this
}