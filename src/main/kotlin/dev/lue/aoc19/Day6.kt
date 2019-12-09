package dev.lue.aoc19

import java.util.*
import kotlin.collections.HashMap

class Day6 : IDay {
    override val part1InputFilename: String = "6.txt"
    override val part2InputFilename: String = "6.txt"

    fun parseInput(raw_input: String, undirected: Boolean = false): Map<String, Set<String>> {
        val map : HashMap<String, MutableSet<String>> = hashMapOf()
        for (row in raw_input.trim().split("\n")) {
            val edge = row.split(")")
            val src = edge[0]
            val dst = edge[1]
            if (!map.containsKey(src)) {
                map[src] = mutableSetOf(dst)
            } else{
                map[src]!!.add(dst)
            }
            if (undirected) {
                if (!map.containsKey(dst)) {
                    map[dst] = mutableSetOf(src)
                } else{
                    map[dst]!!.add(src)
                }
            }
        }
        return map
    }

    override fun runPart1(raw_input: String): Int {
        val graph = parseInput(raw_input)
        //println("$graph")
        return dfs("COM", 0, graph)
    }

    fun dfs(node: String, a: Int, graph: Map<String, Set<String>>): Int {
        var b = 0
        for (nei in graph.getOrDefault(node, emptySet())) {
            b += dfs(nei, a+1, graph)
        }
        return b + a
    }

    data class Node(val depth: Int, val vertex: String)

    override fun runPart2(raw_input: String): Int {
        val graph = parseInput(raw_input, undirected = true)
        //println("$graph")
        // do breadth first search
        val start = "YOU"
        val goal = "SAN"
        val q: Deque<Node> = LinkedList()
        val visited = mutableSetOf<String>()

        q.addLast(Node(0, start))

        while (q.isNotEmpty()) {
            val node = q.pollFirst()
            if (node.vertex == goal) {
                return node.depth - 2
            }
            for (nei in graph.getOrDefault(node.vertex, emptySet())) {
                if (!visited.contains(nei)) {
                    visited.add(nei)
                    val next_node = Node(node.depth+1, nei)
                    q.addLast(next_node)
                }
            }
        }
        println("No path found :(")
        return -1
    }
}