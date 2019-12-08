package dev.lue.aoc19

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class Day2Test{
    // https://blog.oio.de/2018/11/13/how-to-use-junit-5-methodsource-parameterized-tests-with-kotlin/
    companion object {
        @JvmStatic
        fun provideCpuTestArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1,0,0,0,99", listOf(2,0,0,0,99)),
                Arguments.of("2,3,0,3,99", listOf(2,3,0,6,99)),
                Arguments.of("2,4,4,5,99,0", listOf(2,4,4,5,99,9801)),
                Arguments.of("1,1,1,4,99,5,6,0,99", listOf(30,1,1,4,2,5,6,0,99))
            )
    }

    @ParameterizedTest
    @MethodSource("provideCpuTestArguments")
    fun cpuTest(raw_input: String, expectedResult: List<Int>) {
        val app = Day2()
        val program = app.parseInput(raw_input).toMutableList()
        val cpu = CPU(program)
        cpu.run()
        assertEquals(expectedResult, program, "program [$raw_input] should result in $expectedResult")
    }

    @Test
    fun solutionTest() {
        val app = Day2()
        app.run()
    }
}