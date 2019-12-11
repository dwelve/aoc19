package dev.lue.aoc19.Intcode

import dev.lue.aoc19.Day9.CPU
import dev.lue.aoc19.Day9.IOType
import dev.lue.aoc19.Day9.ModeType
import kotlinx.coroutines.channels.Channel
import kotlin.reflect.KSuspendFunction1

typealias IntcodeInt = Long

class HaltException : Exception() {}
class InvalidModeException : Exception() {}

data class Opcode(
    val op: IntcodeInt,
    //val numberOfArguments: IntcodeInt,
    val autoIncrementProgramCounter: Boolean,
    val call: KSuspendFunction1<
            @ParameterName(name = "args") MutableList<IntcodeInt>,
            Unit>,
    val parameters: List<IOType>)

class Processor(val program: MutableList<IntcodeInt>, val inputChannel: Channel<IntcodeInt>, val outputChannel: Channel<IntcodeInt>) {
    val programMemory: MutableMap<Long, Long> = program.mapIndexed { index, value -> index.toLong() to value }.toMap().toMutableMap()
    var programCounter: IntcodeInt = 0
    var relativeBase: IntcodeInt = 0
    val inputDevice = ChannelReader(inputChannel)
    val outputDevice = ChannelWriter(outputChannel)

    val opcodes: Map<IntcodeInt, Opcode> = mapOf(
        1L to Opcode(1, true, ::add, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        2L to Opcode(2, true, ::mul, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        3L to Opcode(3, true, ::readInput, listOf(IOType.WRITE)),
        4L to Opcode(4, true, ::writeOutput, listOf(IOType.READ)),
        5L to Opcode(5, false, ::jumpIfTrue, listOf(IOType.READ, IOType.READ)),
        6L to Opcode(6, false, ::jumpIfFalse, listOf(IOType.READ, IOType.READ)),
        7L to Opcode(7, true, ::isLessThan, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        8L to Opcode(8, true, ::isEqual, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        9L to Opcode(9, true, ::addToRelativeBase, listOf(IOType.READ)),

        99L to Opcode(99, true, ::halt, emptyList())
    )

    suspend fun run() {
        try {
            while (true) {
                step()
            }
        } catch (e: HaltException) {
            println("CPU HALT")
        }
        //println(program)
    }

    suspend fun step() {
        //decode instruction
        val opWithMode: Long = programMemory[programCounter] ?: error("Program counter out of program memory range!")
        val op = opWithMode % 100
        val m0 = ModeType.from(((opWithMode / 100) % 10).toInt())
        val m1 = ModeType.from(((opWithMode / 1000) % 10).toInt())
        val m2 = ModeType.from(((opWithMode / 10000) % 10).toInt())
        val modes = listOf(m0, m1, m2)
        val opcode = opcodes[op] ?: error("Invalid opcode: $op")

        // construct argument list
        val arguments: MutableList<IntcodeInt> = mutableListOf()
        for ((index, ioType) in opcode.parameters.withIndex()) {
            val mode = modes[index]
            val arg = when (ioType) {
                IOType.READ -> {
                    val argValue: IntcodeInt = programMemory[programCounter + 1 + index]!!
                    val value: IntcodeInt = readMemory(argValue, mode)
                    value
                }
                IOType.WRITE -> 0
            }
            arguments.add(arg)
        }

        //println("arguments before: $arguments")
        //println("pc = $programCounter  op = $op  args = $arguments")

        opcode.call(arguments)
        //println("arguments after:  $arguments")


        // if write mode and output is
        for ((index, ioType) in opcode.parameters.withIndex()) {
            if (ioType == IOType.WRITE) {
                val mode = modes[index]
                val indirectAddress: IntcodeInt = programMemory[programCounter + 1 + index]!!
                val value = arguments[index]
                when (mode) {
                    ModeType.INDIRECT -> programMemory[indirectAddress] = value
                    ModeType.IMMEDIATE -> throw InvalidModeException()
                    ModeType.RELATIVE -> programMemory[indirectAddress + relativeBase] = value
                }

            }
        }

        //println("after pc = $programCounter  op = $op  args = $arguments")

        if (opcode.autoIncrementProgramCounter) {
            programCounter += opcode.parameters.size + 1
        }

    }

    fun readMemory(value: IntcodeInt, mode: ModeType): IntcodeInt {
        return when (mode) {
            ModeType.INDIRECT -> programMemory.getOrDefault(value, 0)
            ModeType.IMMEDIATE -> value
            ModeType.RELATIVE -> programMemory.getOrDefault(value + relativeBase, 0)
        }
    }

    suspend fun add(args: MutableList<IntcodeInt>) {
        args[2] = args[0] + args[1]
    }

    suspend fun mul(args: MutableList<IntcodeInt>) {
        args[2] = args[0] * args[1]
    }

    suspend fun readInput(args: MutableList<IntcodeInt>) {
        args[0] = inputDevice.read()
    }

    suspend fun writeOutput(args: MutableList<IntcodeInt>) {
        outputDevice.write(args[0])
    }

    suspend fun jumpIfTrue(args: MutableList<IntcodeInt>) {
        if (args[0] != 0L) {
            programCounter = args[1]
        } else {
            programCounter += args.size + 1
        }
    }

    suspend fun jumpIfFalse(args: MutableList<IntcodeInt>) {
        if (args[0] == 0L) {
            programCounter = args[1]
        } else {
            programCounter += args.size + 1
        }
    }

    suspend fun isLessThan(args: MutableList<IntcodeInt>) {
        args[2] = if (args[0] < args[1]) {
            1
        } else {
            0
        }
    }

    suspend fun isEqual(args: MutableList<IntcodeInt>) {
        args[2] = if (args[0] == args[1]) {
            1
        } else {
            0
        }
    }

    suspend fun addToRelativeBase(args: MutableList<IntcodeInt>) {
        relativeBase += args[0]
    }

    suspend fun halt(args: MutableList<IntcodeInt>) {
        throw HaltException()
    }


}

