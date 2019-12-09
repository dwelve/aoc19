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


fun parseInput(raw_input: String): List<IntcodeInt> {
    return raw_input.trim().split(",").map { it.trim().toLong() }
}


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
}