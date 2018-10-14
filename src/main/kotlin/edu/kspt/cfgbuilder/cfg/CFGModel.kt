package edu.kspt.cfgbuilder.cfg

enum class NodeType {
    BEGIN, FLOW, CONDITION, END, LOOP_BEGIN, BREAK
}

data class Node(val type: NodeType, val text: String, val id: Int = CURRENT_MAX_ID++) {
    companion object {
        var CURRENT_MAX_ID = 0
    }

    fun connectTo(cfg: ControlFlowGraph, linkText: String = "", phantom: Boolean = false): ControlFlowGraph {
        return if (cfg.isEmpty()) {
            mapOf(this to emptySet())
        } else {
            cfg + mapOf(this to setOf(LinkTo(cfg.findStart(), linkText, phantom)))
        }
    }

    fun join(firstCfg: ControlFlowGraph, secondCfg: ControlFlowGraph,
             firstLinkText: String = "", secondLinkText: String = ""): ControlFlowGraph {
        val startOfFirstCfg = firstCfg.findStart()
        val startOfSecondCfg = secondCfg.findStart()
        val jointCfg = (firstCfg + secondCfg).toMutableMap()
        jointCfg[this] = setOf(
                LinkTo(startOfFirstCfg, firstLinkText),
                LinkTo(startOfSecondCfg, secondLinkText))
        return jointCfg
    }
}

data class LinkTo(val otherNode: Node, val text: String = "", val phantom: Boolean = false)

typealias ControlFlowGraph = Map<Node, Set<LinkTo>>

fun emptyCfg(): ControlFlowGraph = emptyMap()

fun ControlFlowGraph.findStart(): Node {
    return this.keys.single { node ->
        node !in this.values.flatMap { it }.filter { it.phantom == false }.map { it.otherNode }
    }
}

fun ControlFlowGraph.findEnds(predicate: (Node) -> Boolean) = this.filter { (k, v) -> predicate(k) && v.isEmpty() }.keys

fun ControlFlowGraph.findAllEnds() = this.findEnds { true }

fun ControlFlowGraph.findNonBreakEnds() = this.findEnds { it.type != NodeType.BREAK }

fun ControlFlowGraph.findBreakEnds() = this.findEnds { it.type == NodeType.BREAK }

fun ControlFlowGraph.appendNode(node: Node): ControlFlowGraph {
    val newGraphEntries = this.findAllEnds().map { it to setOf(LinkTo(node)) }.toMap()
    val newEnd = node to emptySet<LinkTo>()
    return this + newGraphEntries + newEnd
}

fun ControlFlowGraph.concat(other: ControlFlowGraph): ControlFlowGraph {
    val newGraphEntries = this.findAllEnds().map { it to setOf(LinkTo(other.findStart())) }.toMap()
    return this + newGraphEntries + other
}

fun ControlFlowGraph.prettyPrint() {
    this.forEach { node, links -> println("$node: $links") }
}