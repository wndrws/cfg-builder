package edu.kspt.cfgbuilder.dot

import edu.kspt.cfgbuilder.cfg.ControlFlowGraph
import edu.kspt.cfgbuilder.cfg.LinkTo
import edu.kspt.cfgbuilder.cfg.Node
import edu.kspt.cfgbuilder.cfg.NodeType
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser
import org.junit.jupiter.api.Test
import java.io.File

class DOTModelTest {
    @Test
    fun test() {
        // given
        val cfg = getExampleCfg()
        // when
        val dot = cfg.asDOT("example")
        // then
        println(dot)
        Graphviz.fromGraph(Parser.read(dot))
                .height(1080)
                .render(Format.PNG)
                .toFile(File("examples/ex4-1.png"))
    }

    private fun getExampleCfg(): ControlFlowGraph {
        val beginNode = Node(NodeType.BEGIN, "Find Primes in Range", 11)
        val node1 = Node(NodeType.FLOW, "lower=900", 9)
        val node2 = Node(NodeType.FLOW, "upper=1000", 8)
        val node3 = Node(NodeType.FLOW, "print(\"Prime numbers between\",lower,\"and\",upper,\"are:\")", 7)
        val outerFor = Node(NodeType.LOOP_BEGIN, "num in range(lower,upper+1)", 0)
        val outerIf = Node(NodeType.CONDITION, "num>1", 1)
        val innerFor = Node(NodeType.LOOP_BEGIN, "i in range(2,num)", 3)
        val innerIf = Node(NodeType.CONDITION, "(num%i)==0", 4)
        val breakNode = Node(NodeType.BREAK, "break", 5)
        val elseNode = Node(NodeType.FLOW, "print(num)", 6)
        val nodeAfterForElse = Node(NodeType.FLOW, "print(\"done!\")", 2)
        val endNode = Node(NodeType.END, "return", 10)
        return mapOf(
                beginNode to setOf(LinkTo(node1)),
                node1 to setOf(LinkTo(node2)),
                node2 to setOf(LinkTo(node3)),
                node3 to setOf(LinkTo(outerFor)),
                outerFor to setOf(LinkTo(outerIf, "yes"), LinkTo(endNode, "no")),
                outerIf to setOf(LinkTo(innerFor, "yes"), LinkTo(outerFor, "no", phantom = true)),
                innerFor to setOf(LinkTo(innerIf, "yes"), LinkTo(elseNode, "no")),
                innerIf to setOf(LinkTo(breakNode, "yes"), LinkTo(innerFor, "no", phantom = true)),
                breakNode to setOf(LinkTo(nodeAfterForElse, phantom = true)),
                elseNode to setOf(LinkTo(nodeAfterForElse)),
                nodeAfterForElse to setOf(LinkTo(outerFor, phantom = true)),
                endNode to emptySet()
        )
    }
}