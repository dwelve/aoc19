package dev.lue.aoc19.Intcode

import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

enum class Direction {
    NORTH, SOUTH, WEST, EAST
}

class Day11Test {
    val raw_input = """3,8,1005,8,314,1106,0,11,0,0,0,104,1,104,0,3,8,1002,8,-1,10,1001,10,1,10,4,10,108,1,8,10,4,10,1002,8,1,28,2,2,16,10,1,1108,7,10,1006,0,10,1,5,14,10,3,8,102,-1,8,10,101,1,10,10,4,10,108,1,8,10,4,10,102,1,8,65,1006,0,59,2,109,1,10,1006,0,51,2,1003,12,10,3,8,102,-1,8,10,1001,10,1,10,4,10,108,1,8,10,4,10,1001,8,0,101,1006,0,34,1,1106,0,10,1,1101,17,10,3,8,102,-1,8,10,101,1,10,10,4,10,1008,8,0,10,4,10,1001,8,0,135,3,8,1002,8,-1,10,101,1,10,10,4,10,108,0,8,10,4,10,1001,8,0,156,3,8,1002,8,-1,10,101,1,10,10,4,10,108,0,8,10,4,10,1001,8,0,178,1,108,19,10,3,8,102,-1,8,10,101,1,10,10,4,10,108,0,8,10,4,10,1002,8,1,204,1,1006,17,10,3,8,102,-1,8,10,101,1,10,10,4,10,108,1,8,10,4,10,102,1,8,230,1006,0,67,1,103,11,10,1,1009,19,10,1,109,10,10,3,8,102,-1,8,10,101,1,10,10,4,10,1008,8,0,10,4,10,101,0,8,268,3,8,102,-1,8,10,101,1,10,10,4,10,1008,8,1,10,4,10,1002,8,1,290,2,108,13,10,101,1,9,9,1007,9,989,10,1005,10,15,99,109,636,104,0,104,1,21101,48210224024,0,1,21101,0,331,0,1105,1,435,21101,0,937264165644,1,21101,0,342,0,1105,1,435,3,10,104,0,104,1,3,10,104,0,104,0,3,10,104,0,104,1,3,10,104,0,104,1,3,10,104,0,104,0,3,10,104,0,104,1,21101,235354025051,0,1,21101,389,0,0,1105,1,435,21102,29166169280,1,1,21102,400,1,0,1105,1,435,3,10,104,0,104,0,3,10,104,0,104,0,21102,709475849060,1,1,21102,1,423,0,1106,0,435,21102,868498428684,1,1,21101,434,0,0,1105,1,435,99,109,2,21201,-1,0,1,21101,0,40,2,21102,1,466,3,21101,456,0,0,1105,1,499,109,-2,2105,1,0,0,1,0,0,1,109,2,3,10,204,-1,1001,461,462,477,4,0,1001,461,1,461,108,4,461,10,1006,10,493,1101,0,0,461,109,-2,2106,0,0,0,109,4,2102,1,-1,498,1207,-3,0,10,1006,10,516,21102,1,0,-3,21201,-3,0,1,21201,-2,0,2,21102,1,1,3,21102,535,1,0,1106,0,540,109,-4,2106,0,0,109,5,1207,-3,1,10,1006,10,563,2207,-4,-2,10,1006,10,563,21202,-4,1,-4,1106,0,631,21201,-4,0,1,21201,-3,-1,2,21202,-2,2,3,21101,582,0,0,1105,1,540,22102,1,1,-4,21102,1,1,-1,2207,-4,-2,10,1006,10,601,21101,0,0,-1,22202,-2,-1,-2,2107,0,-3,10,1006,10,623,22102,1,-1,1,21101,623,0,0,105,1,498,21202,-2,-1,-2,22201,-4,-2,-4,109,-5,2105,1,0"""


    val field: MutableMap<Pair<Int, Int>, IntcodeInt> = mutableMapOf()
    val changes = mutableSetOf<Pair<Int, Int>>()
    var x = 0
    var y = 0
    var facing = Direction.NORTH

    suspend fun provideInput(cpu: Processor,
                             inputChannel: Channel<IntcodeInt>,
                             outputChannel: Channel<IntcodeInt>) {
        while (true) {
            println("Waiting for ship to send output")
            val output = outputChannel.receive()
            println("Recieved output $output")
            changes.add(Pair(x, y))
            field[Pair(x, y)] = output

            println("Waiting for ship to send direction")
            val direction = outputChannel.receive()
            println("Got direction $direction")
            if (direction == 0L) {
                when (facing) {
                    Direction.NORTH -> {
                        x -= 1
                        facing = Direction.WEST
                    }
                    Direction.EAST -> {
                        y += 1
                        facing = Direction.NORTH
                    }
                    Direction.SOUTH -> {
                        x += 1
                        facing = Direction.EAST
                    }
                    Direction.WEST -> {
                        y -= 1
                        facing = Direction.SOUTH
                    }
                }
            } else {
                when (facing) {
                    Direction.NORTH -> {
                        x += 1
                        facing = Direction.EAST
                    }
                    Direction.EAST -> {
                        y -= 1
                        facing = Direction.SOUTH
                    }
                    Direction.SOUTH -> {
                        x -= 1
                        facing = Direction.WEST
                    }
                    Direction.WEST -> {
                        y += 1
                        facing = Direction.NORTH
                    }
                }
            }
            val current = field.getOrDefault(Pair(x, y), 0)
            println("provide input: $x, $y -> $current")
            cpu.inputDevice.feedInput(current)
        }
    }

    fun runDraw(program: MutableList<IntcodeInt>): IntcodeInt {
        val output = runBlocking {

            val inputChannel= Channel<IntcodeInt>(Channel.UNLIMITED)
            val outputChannel= Channel<IntcodeInt>(Channel.UNLIMITED)

            val cpu = Processor(program.toMutableList(), inputChannel, outputChannel)
            // cpu0 has initial input of 0

            field[Pair(0,0)] = 1L
            cpu.inputDevice.feedInput(1L)

            val job = async {
                cpu.run()
            }
            val job2 = launch {
                provideInput(cpu, inputChannel, outputChannel)
            }
                //lastOutput = cpu.outputDevice.buffer.pop()
            job.join()  // only care about last stage
            job2.cancelAndJoin()

            val foo = changes.size.toLong()
            println("answer is $foo")
            println("field is:")
            println("$field")
            println()
            println()
            for ((key, value) in field) {
                println("${key.first}, ${key.second}, $value")
            }

            return@runBlocking 1
        }
        return changes.size.toLong()
    }

    @Test
    fun testIt() {
        val program = parseInput(raw_input).toMutableList()
        val answer = runDraw(program)
        println("answer: $answer")
    }


}