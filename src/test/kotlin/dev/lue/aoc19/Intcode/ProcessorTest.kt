package dev.lue.aoc19.Intcode

import dev.lue.aoc19.Day2.CPU
import dev.lue.aoc19.Day2.Day2
import dev.lue.aoc19.Day9.Day9
import javafx.application.Application.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class ProcessorTest {
    companion object {
        @JvmStatic
        fun provideProcessorBasicTestArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1,0,0,0,99", listOf<IntcodeInt>(2,0,0,0,99)),
                Arguments.of("2,3,0,3,99", listOf<IntcodeInt>(2,3,0,6,99)),
                Arguments.of("2,4,4,5,99,0", listOf<IntcodeInt>(2,4,4,5,99,9801)),
                Arguments.of("1,1,1,4,99,5,6,0,99", listOf<IntcodeInt>(30,1,1,4,2,5,6,0,99))
            )

        @JvmStatic
        fun provideProcessorIOTestArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1002,4,3,4,33", listOf<IntcodeInt>(1002,4,3,4,99))
            )

        @JvmStatic
        fun provideDay5Arguments() : Stream<Arguments> =
            Stream.of(
                Arguments.of("3,225,1,225,6,6,1100,1,238,225,104,0,1101,33,37,225,101,6,218,224,1001,224,-82,224,4,224,102,8,223,223,101,7,224,224,1,223,224,223,1102,87,62,225,1102,75,65,224,1001,224,-4875,224,4,224,1002,223,8,223,1001,224,5,224,1,224,223,223,1102,49,27,225,1101,6,9,225,2,69,118,224,101,-300,224,224,4,224,102,8,223,223,101,6,224,224,1,224,223,223,1101,76,37,224,1001,224,-113,224,4,224,1002,223,8,223,101,5,224,224,1,224,223,223,1101,47,50,225,102,43,165,224,1001,224,-473,224,4,224,102,8,223,223,1001,224,3,224,1,224,223,223,1002,39,86,224,101,-7482,224,224,4,224,102,8,223,223,1001,224,6,224,1,223,224,223,1102,11,82,225,1,213,65,224,1001,224,-102,224,4,224,1002,223,8,223,1001,224,6,224,1,224,223,223,1001,14,83,224,1001,224,-120,224,4,224,1002,223,8,223,101,1,224,224,1,223,224,223,1102,53,39,225,1101,65,76,225,4,223,99,0,0,0,677,0,0,0,0,0,0,0,0,0,0,0,1105,0,99999,1105,227,247,1105,1,99999,1005,227,99999,1005,0,256,1105,1,99999,1106,227,99999,1106,0,265,1105,1,99999,1006,0,99999,1006,227,274,1105,1,99999,1105,1,280,1105,1,99999,1,225,225,225,1101,294,0,0,105,1,0,1105,1,99999,1106,0,300,1105,1,99999,1,225,225,225,1101,314,0,0,106,0,0,1105,1,99999,1107,677,226,224,1002,223,2,223,1005,224,329,101,1,223,223,8,677,226,224,102,2,223,223,1006,224,344,1001,223,1,223,108,677,677,224,1002,223,2,223,1006,224,359,1001,223,1,223,1108,226,677,224,102,2,223,223,1006,224,374,1001,223,1,223,1008,677,226,224,102,2,223,223,1005,224,389,101,1,223,223,7,226,677,224,102,2,223,223,1005,224,404,1001,223,1,223,1007,677,677,224,1002,223,2,223,1006,224,419,101,1,223,223,107,677,226,224,102,2,223,223,1006,224,434,101,1,223,223,7,677,677,224,1002,223,2,223,1005,224,449,101,1,223,223,108,677,226,224,1002,223,2,223,1006,224,464,101,1,223,223,1008,226,226,224,1002,223,2,223,1006,224,479,101,1,223,223,107,677,677,224,1002,223,2,223,1006,224,494,1001,223,1,223,1108,677,226,224,102,2,223,223,1005,224,509,101,1,223,223,1007,226,677,224,102,2,223,223,1005,224,524,1001,223,1,223,1008,677,677,224,102,2,223,223,1005,224,539,1001,223,1,223,1107,677,677,224,1002,223,2,223,1006,224,554,1001,223,1,223,1007,226,226,224,1002,223,2,223,1005,224,569,1001,223,1,223,7,677,226,224,1002,223,2,223,1006,224,584,1001,223,1,223,108,226,226,224,102,2,223,223,1005,224,599,1001,223,1,223,8,677,677,224,102,2,223,223,1005,224,614,1001,223,1,223,1107,226,677,224,102,2,223,223,1005,224,629,1001,223,1,223,8,226,677,224,102,2,223,223,1006,224,644,1001,223,1,223,1108,226,226,224,1002,223,2,223,1006,224,659,101,1,223,223,107,226,226,224,1002,223,2,223,1006,224,674,1001,223,1,223,4,223,99,226", 1L, 16209841L),
                Arguments.of("3,225,1,225,6,6,1100,1,238,225,104,0,1101,33,37,225,101,6,218,224,1001,224,-82,224,4,224,102,8,223,223,101,7,224,224,1,223,224,223,1102,87,62,225,1102,75,65,224,1001,224,-4875,224,4,224,1002,223,8,223,1001,224,5,224,1,224,223,223,1102,49,27,225,1101,6,9,225,2,69,118,224,101,-300,224,224,4,224,102,8,223,223,101,6,224,224,1,224,223,223,1101,76,37,224,1001,224,-113,224,4,224,1002,223,8,223,101,5,224,224,1,224,223,223,1101,47,50,225,102,43,165,224,1001,224,-473,224,4,224,102,8,223,223,1001,224,3,224,1,224,223,223,1002,39,86,224,101,-7482,224,224,4,224,102,8,223,223,1001,224,6,224,1,223,224,223,1102,11,82,225,1,213,65,224,1001,224,-102,224,4,224,1002,223,8,223,1001,224,6,224,1,224,223,223,1001,14,83,224,1001,224,-120,224,4,224,1002,223,8,223,101,1,224,224,1,223,224,223,1102,53,39,225,1101,65,76,225,4,223,99,0,0,0,677,0,0,0,0,0,0,0,0,0,0,0,1105,0,99999,1105,227,247,1105,1,99999,1005,227,99999,1005,0,256,1105,1,99999,1106,227,99999,1106,0,265,1105,1,99999,1006,0,99999,1006,227,274,1105,1,99999,1105,1,280,1105,1,99999,1,225,225,225,1101,294,0,0,105,1,0,1105,1,99999,1106,0,300,1105,1,99999,1,225,225,225,1101,314,0,0,106,0,0,1105,1,99999,1107,677,226,224,1002,223,2,223,1005,224,329,101,1,223,223,8,677,226,224,102,2,223,223,1006,224,344,1001,223,1,223,108,677,677,224,1002,223,2,223,1006,224,359,1001,223,1,223,1108,226,677,224,102,2,223,223,1006,224,374,1001,223,1,223,1008,677,226,224,102,2,223,223,1005,224,389,101,1,223,223,7,226,677,224,102,2,223,223,1005,224,404,1001,223,1,223,1007,677,677,224,1002,223,2,223,1006,224,419,101,1,223,223,107,677,226,224,102,2,223,223,1006,224,434,101,1,223,223,7,677,677,224,1002,223,2,223,1005,224,449,101,1,223,223,108,677,226,224,1002,223,2,223,1006,224,464,101,1,223,223,1008,226,226,224,1002,223,2,223,1006,224,479,101,1,223,223,107,677,677,224,1002,223,2,223,1006,224,494,1001,223,1,223,1108,677,226,224,102,2,223,223,1005,224,509,101,1,223,223,1007,226,677,224,102,2,223,223,1005,224,524,1001,223,1,223,1008,677,677,224,102,2,223,223,1005,224,539,1001,223,1,223,1107,677,677,224,1002,223,2,223,1006,224,554,1001,223,1,223,1007,226,226,224,1002,223,2,223,1005,224,569,1001,223,1,223,7,677,226,224,1002,223,2,223,1006,224,584,1001,223,1,223,108,226,226,224,102,2,223,223,1005,224,599,1001,223,1,223,8,677,677,224,102,2,223,223,1005,224,614,1001,223,1,223,1107,226,677,224,102,2,223,223,1005,224,629,1001,223,1,223,8,226,677,224,102,2,223,223,1006,224,644,1001,223,1,223,1108,226,226,224,1002,223,2,223,1006,224,659,101,1,223,223,107,226,226,224,1002,223,2,223,1006,224,674,1001,223,1,223,4,223,99,226", 5L, 8834787L)
            )

        @JvmStatic
        fun provideJumpAndComparisonTestArguments() : Stream<Arguments> =
            Stream.of(
                Arguments.of("3,9,8,9,10,9,4,9,99,-1,8", 8L, 1L),
                Arguments.of("3,9,8,9,10,9,4,9,99,-1,8", 7L, 0L),
                Arguments.of("3,3,1108,-1,8,3,4,3,99", 8L, 1L),
                Arguments.of("3,3,1108,-1,8,3,4,3,99", 7L, 0L),
                Arguments.of("3,9,7,9,10,9,4,9,99,-1,8", 8L, 0L),
                Arguments.of("3,9,7,9,10,9,4,9,99,-1,8", 7L, 1L),
                Arguments.of("3,3,1107,-1,8,3,4,3,99", 8L, 0L),
                Arguments.of("3,3,1107,-1,8,3,4,3,99", 7L, 1L),
                Arguments.of("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", 0L, 0L),
                Arguments.of("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", 5L, 1L),
                Arguments.of("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", 0L, 0L),
                Arguments.of("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", 5L, 1L),
                Arguments.of("""3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99""", 5L, 999L),
                Arguments.of("""3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99""", 8L, 1000L),
                Arguments.of("""3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99""", 23L, 1001L)
            )
    }

    @ParameterizedTest
    @MethodSource("provideProcessorBasicTestArguments")
    fun processerBasicTests(raw_input: String, expectedResult: List<IntcodeInt>) {
        val program = parseInput(raw_input).toMutableList()
        val inputChannel = Channel<IntcodeInt>(UNLIMITED)
        val outputChannel = Channel<IntcodeInt>(UNLIMITED)
        val cpu = Processor(program, inputChannel, outputChannel)
        val output: List<IntcodeInt> = runBlocking {
            val job = async {
                cpu.run()
            }
            job.join()
            return@runBlocking cpu.programMemory.values.toList()
        }
        assertEquals(expectedResult, output, "program [$raw_input] should result in $expectedResult")
    }

    @ParameterizedTest
    @MethodSource("provideProcessorIOTestArguments")
    fun processerIOTests(raw_input: String, expectedResult: List<IntcodeInt>) {
        val program = parseInput(raw_input).toMutableList()
        val inputChannel = Channel<IntcodeInt>(UNLIMITED)
        val outputChannel = Channel<IntcodeInt>(UNLIMITED)
        val cpu = Processor(program, inputChannel, outputChannel)
        val output: List<IntcodeInt> = runBlocking {
            val job = async {
                cpu.run()
            }
            job.join()
            return@runBlocking cpu.programMemory.values.toList()
        }
        assertEquals(expectedResult, output, "program [$raw_input] should result in $expectedResult")
    }

    @ParameterizedTest
    @MethodSource("provideDay5Arguments")
    fun day5Test(raw_input: String, input: IntcodeInt, expectedResult: IntcodeInt) {
        val program = parseInput(raw_input).toMutableList()
        val inputChannel = Channel<IntcodeInt>(UNLIMITED)
        val outputChannel = Channel<IntcodeInt>(UNLIMITED)
        val cpu = Processor(program, inputChannel, outputChannel)
        val output: List<IntcodeInt> = runBlocking {
            cpu.inputDevice.feedInput(input)
            val job = async {
                cpu.run()
            }
            job.join()
            return@runBlocking cpu.outputDevice.buffer.toList()
        }

        val lastOutput = output.last()
        assertEquals(expectedResult, lastOutput, "program [$raw_input] should result in $expectedResult")

        // make sure diagnostic tests succeeded
        val tests = output.dropLast(1)
        assertEquals(0, tests.sum())
    }

    @ParameterizedTest
    @MethodSource("provideJumpAndComparisonTestArguments")
    fun jumpAndComparisonTests(raw_input: String, input: IntcodeInt, expectedResult: IntcodeInt) {
        val program = parseInput(raw_input).toMutableList()
        val inputChannel = Channel<IntcodeInt>(UNLIMITED)
        val outputChannel = Channel<IntcodeInt>(UNLIMITED)
        val cpu = Processor(program, inputChannel, outputChannel)
        val output: List<IntcodeInt> = runBlocking {
            cpu.inputDevice.feedInput(input)
            val job = async {
                cpu.run()
            }
            job.join()
            return@runBlocking cpu.outputDevice.buffer.toList()
        }

        val lastOutput = output.last()
        assertEquals(expectedResult, lastOutput)

        assertEquals(1, output.size)
    }
}