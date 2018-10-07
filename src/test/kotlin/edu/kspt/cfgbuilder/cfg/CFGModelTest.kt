package edu.kspt.cfgbuilder.cfg

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CFGModelTest {

    @Test
    fun `can connect a node to CFG beginning`() {
        // given
        val (begin, cfg) = createCfgWithIfStatement()
        val originalCfg = cfg.toMap()
        val newBegin = Node(NodeType.BEGIN, "newBegin")
        // when
        newBegin.connectTo(cfg)
        // then
        assertThat(cfg).containsKey(newBegin)
        assertThat(cfg[newBegin]).containsOnly(LinkTo(begin))
        assertThat(cfg.without(newBegin)).isEqualTo(originalCfg)
    }

    private fun createCfgWithIfStatement(): Pair<Node, ControlFlowGraph> {
        val begin = Node(NodeType.FLOW, "begin")
        val condition = Node(NodeType.CONDITION, "condition")
        val trueBranch = Node(NodeType.FLOW, "trueBranch")
        val falseBranch = Node(NodeType.FLOW, "falseBranch")
        val end = Node(NodeType.END, "end")
        val cfg: ControlFlowGraph = mutableMapOf(
                begin to setOf(LinkTo(condition)),
                condition to setOf(LinkTo(trueBranch, "yes"), LinkTo(falseBranch, "no")),
                trueBranch to setOf(LinkTo(end)),
                falseBranch to setOf(LinkTo(end)),
                end to emptySet())
        return Pair(begin, cfg)
    }

    private fun ControlFlowGraph.without(node: Node): ControlFlowGraph {
        val copy = this.toMutableMap()
        copy.remove(node)
        return copy
    }

    @Test
    fun `can join two CFGs at a node`() {
        // given
        val (begin1, cfg1) = createPlanarCfg("1")
        val (begin2, cfg2) = createPlanarCfg("2")
        val newBegin = Node(NodeType.BEGIN, "newBegin")
        // when
        val jointCfg = newBegin.join(cfg1, cfg2)
        // then
        assertThat(jointCfg).containsKey(newBegin)
        assertThat(jointCfg[newBegin]).containsExactlyInAnyOrder(LinkTo(begin1), LinkTo(begin2))
        val ex = assertThrows<IllegalArgumentException> { jointCfg.without(newBegin).findStart() }
        assertThat(ex.message).isEqualTo("Collection contains more than one matching element.")
    }

    private fun createPlanarCfg(namingParam: String): Triple<Node, ControlFlowGraph, Node> {
        val begin = Node(NodeType.FLOW, "beginNode$namingParam")
        val node2 = Node(NodeType.FLOW, "middleNode$namingParam")
        val node3 = Node(NodeType.FLOW, "endNode$namingParam")
        val cfg: ControlFlowGraph = mutableMapOf(
                begin to setOf(LinkTo(node2)),
                node2 to setOf(LinkTo(node3)),
                node3 to emptySet())
        return Triple(begin, cfg, node3)
    }

    @Test
    fun `can find ends of CFG`() {
        // given
        val (_, cfg1, end1) = createPlanarCfg("1")
        val (_, cfg2, end2) = createPlanarCfg("2")
        val newBegin = Node(NodeType.BEGIN, "newBegin")
        val jointCfg = newBegin.join(cfg1, cfg2)
        // when
        val ends = jointCfg.findAllEnds()
        // then
        assertThat(ends).containsExactlyInAnyOrder(end1, end2)
    }
}