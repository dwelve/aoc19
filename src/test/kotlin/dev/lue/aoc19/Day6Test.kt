package dev.lue.aoc19

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class Day6Test{

    companion object {
        @JvmStatic
        fun provideCpuTestArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of("""COM)B
B)C
B)D
C)D
D)E""", mapOf("COM" to setOf("B"),
                    "B" to setOf("C", "D"),
                    "C" to setOf("D"),
                    "D" to setOf("E")
                    )
            ))
    }

    @ParameterizedTest
    @MethodSource("provideCpuTestArguments")
    fun parseInputTest(raw_input: String, expectedResult: Map<String, Set<String>>) {
        val app = Day6()
        val graph = app.parseInput(raw_input)
        assertEquals(expectedResult, graph)
    }

    @Test
    fun examplePart1Test() {
        val raw_input = """COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L"""
        val app = Day6()
        val ans = app.runPart1(raw_input)
        assertEquals(42, ans)
    }

    @Test
    fun examplePart2Test() {
        val raw_input = """COM)B
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
K)YOU
I)SAN"""
        val app = Day6()
        val ans = app.runPart2(raw_input)
        assertEquals(4, ans)
    }

    @Test
    fun solutionTest() {
        val app = Day6()
        app.run()
    }
}