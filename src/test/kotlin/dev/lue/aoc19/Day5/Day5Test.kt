package dev.lue.aoc19.Day5

import dev.lue.aoc19.Day5.CPU
import dev.lue.aoc19.Day5.Day5

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class Day5Test{
    // https://blog.oio.de/2018/11/13/how-to-use-junit-5-methodsource-parameterized-tests-with-kotlin/
    companion object {
        @JvmStatic
        fun provideCpuTestArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1002,4,3,4,33", listOf(1002,4,3,4,99))
            )
    }

    @ParameterizedTest
    @MethodSource("provideCpuTestArguments")
    fun cpuTest(raw_input: String, expectedResult: List<Int>) {
        val app = Day5()
        val program = app.parseInput(raw_input).toMutableList()
        val cpu = CPU(program)
        cpu.run()
        assertEquals(expectedResult, program, "program [$raw_input] should result in $expectedResult")
    }


    @Test
    fun solutionTest() {
        val app = Day5()
        app.run()
    }
}