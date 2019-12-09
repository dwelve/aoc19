package dev.lue.aoc19

class Day6 : IDay {
    override val part1InputFilename: String = "6.txt"
    override val part2InputFilename: String = "6.txt"

    fun parseInput(raw_input: String): Map<String, Set<String>> {
        /*
COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L
         */
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
        }
        return map
    }

    override fun runPart1(raw_input: String): Int {
        val graph = parseInput(raw_input)
        println("$graph")
        return dfs("COM", 0, graph)
    }

    fun dfs(node: String, a: Int, graph: Map<String, Set<String>>): Int {
        var b = 0
        for (nei in graph.getOrDefault(node, emptySet())) {
            b += dfs(nei, a+1, graph)
        }
        return b + a
    }

    override fun runPart2(raw_input: String): Int {
        return -1
    }
}