package dev.lue.aoc19.Day7

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class Day7Test {
    //@Test
    fun solutionTest() {
        val app = Day7()
        app.run()
    }

    @Test
    fun getPermutationsTest() {
        val input = listOf(0, 1, 2)
        val expected = listOf(
            listOf(0, 1, 2),
            listOf(0, 2, 1),
            listOf(1, 0, 2),
            listOf(1, 2, 0),
            listOf(2, 0, 1),
            listOf(2, 1, 0)
        )
        val output = getPermutations(input)
        assertEquals(expected.toSet(), output.toSet())
    }
}