package dev.lue.aoc19.Day5

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
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

        @JvmStatic
        fun provideCpuTestArguments2(): Stream<Arguments> =
            Stream.of(
                Arguments.of("3,9,8,9,10,9,4,9,99,-1,8", 8, 1),
                Arguments.of("3,9,7,9,10,9,4,9,99,-1,8", 8, 0),
                Arguments.of("3,3,1108,-1,8,3,4,3,99", 8, 1),
                Arguments.of("3,3,1107,-1,8,3,4,3,99", 8, 0),
                Arguments.of("3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99", 7, 999),
                Arguments.of("3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99", 8, 1000),
                Arguments.of("3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99", 9, 1001),
                Arguments.of("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", 0, 0),
                Arguments.of("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", 5, 1),
                Arguments.of("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", 0, 0),
                Arguments.of("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", 5, 1)
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

    /*
    tests = [
    ([3,9,8,9,10,9,4,9,99,-1,8], [8], [1]),
    ([3,9,7,9,10,9,4,9,99,-1,8], [8], [0]),
    ([3,3,1108,-1,8,3,4,3,99], [8], [1]),
    ([3,3,1107,-1,8,3,4,3,99], [8], [0]),
    ([3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,
1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,
999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99], [7], [999]),
    ([3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,
1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,
999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99], [8], [1000]),
    ([3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,
1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,
999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99], [9], [1001]),
    ([3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9], [0], [0]),
    ([3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9], [5], [1]),
    ([3,3,1105,-1,9,1101,0,0,12,4,12,99,1], [0], [0]),
    ([3,3,1105,-1,9,1101,0,0,12,4,12,99,1], [5], [1]),
]
     */

    @ParameterizedTest
    @MethodSource("provideCpuTestArguments2")
    fun cpuTest2(raw_input: String, input: Int, expectedResult: Int) {
        val app = Day5()
        val program = app.parseInput(raw_input).toMutableList()
        val cpu = CPU(program)
        cpu.inputDevice.feedInput(input)
        cpu.run()
        val output = cpu.outputDevice.buffer.toList()
        assertEquals(1, output.size)
        assertEquals(expectedResult, output[0])

    }


    @Test
    fun solutionTest() {
        val app = Day5()
        app.run()
    }
}