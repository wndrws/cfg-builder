package edu.kspt.cfgbuilder.cfg

enum class NodeType {
    BEGIN, FLOW, CONDITION, END
}

data class Node(val type: NodeType, val text: String, val id: Int = CURRENT_MAX_ID++) {
    companion object {
        private var CURRENT_MAX_ID = 0
    }

    fun connectTo(cfg: ControlFlowGraph, linkText: String = "") {
        cfg[this] = if (cfg.isEmpty()) {
            emptySet()
        } else {
            setOf(LinkTo(cfg.findStart(), linkText))
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

data class LinkTo(val otherNode: Node, val text: String = "")

typealias ControlFlowGraph = MutableMap<Node, Set<LinkTo>>

fun ControlFlowGraph.findStart(): Node {
    return this.keys.single { node ->
        node !in this.values.flatMap { it }.map { it.otherNode }
    }
}

fun ControlFlowGraph.findAllEnds() = this.filterValues { it.isEmpty() }.keys