package dev.lue.aoc19.Day9

import dev.lue.aoc19.IDay
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.reflect.KSuspendFunction3

enum class IOType { READ, WRITE }
enum class ModeType(val mode: Int) {
    INDIRECT(0),
    LITERAL(1),
    RELATIVE(2);

    companion object {
        fun from(findMode: Int): ModeType = ModeType.values().first { it.mode == findMode }
    }
}

//typealias OpcodeFunction = (MutableMap<Long, Long>, Int, List<ModeType>) -> Int
//typealias IOFunction = (MutableList<Int>, Int, ModeType) -> ()

data class Opcode(
    val op: Long,
    val call: KSuspendFunction3<
            @ParameterName(name = "data") MutableMap<Long, Long>,
            @ParameterName(name = "pc") Long,
            @ParameterName(name = "modes") List<ModeType>,
            Long>,
    val parameters: List<IOType>)


interface IReadDevice {
    suspend fun read(): Long
}

interface IWriteDevice {
    suspend fun write(value: Long)
}

interface IReadWriteDevice: IReadDevice, IWriteDevice

class IntcodeInput(val channel: Channel<Long>): IReadDevice {
    val buffer: Deque<Long> = LinkedList()

    override suspend fun read(): Long {
        val value = channel.receive()
        buffer.addLast(value)
        return value
    }

    suspend fun feedInput(value: Long) {
        channel.send(value)
    }
}

class IntcodeOutput(val channel: Channel<Long>): IWriteDevice {
    val buffer: Deque<Long> = LinkedList()

    override suspend fun write(value: Long) {
        buffer.addLast(value)
        channel.send(value)
    }
}

class CPU constructor(val _program: MutableList<Long>, val inputChannel: Channel<Long>, val outputChannel: Channel<Long>) {
    val programMemory: MutableMap<Long, Long> = _program.mapIndexed { index, value -> index.toLong() to value }.toMap().toMutableMap()
    var pc: Long = 0  // program counter
    var rb: Long = 0  // relative base
    val inputDevice = IntcodeInput(inputChannel)
    val outputDevice = IntcodeOutput(outputChannel)

    val opcodes: Map<Long, Opcode > = mapOf<Long, Opcode>(
        1L to Opcode(1, ::op1, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        2L to Opcode(2, ::op2, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        3L to Opcode(3, ::op3, listOf(IOType.READ, IOType.WRITE)),
        4L to Opcode(4, ::op4, listOf(IOType.READ)),
        5L to Opcode(5, ::op5, listOf(IOType.READ, IOType.READ)),
        6L to Opcode(6, ::op6, listOf(IOType.READ, IOType.READ)),
        7L to Opcode(7, ::op7, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        8L to Opcode(8, ::op8, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        9L to Opcode(9, ::op9, listOf(IOType.READ)),

        99L to Opcode(99, ::op99, emptyList())
    )

    suspend fun op1(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // add
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a + b
        //println("$a + $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op2(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // mul
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a * b
        //println("$a * $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op3(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // read from input, write to  location
        if (modes[0] == ModeType.LITERAL) {
            throw InvalidModeException()
        }
        val a = inputDevice.read()
        //println("$a * $b = $out")
        write(data, pc+1, a, modes[0])
        return pc+2
    }

    suspend fun op4(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // write to output
        val a = read(data, pc+1, modes[0])
        outputDevice.write(a)
        return pc+2
    }

    suspend fun op5(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // jump-if-true
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0L -> pc + 3
            else -> read(data, pc+2, modes[1])
        }
    }

    suspend fun op6(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // jump-if-false
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0L -> read(data, pc+2, modes[1])
            else -> pc + 3
        }
    }

    suspend fun op7(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // is-less-than
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out: Long = when (a < b) {
            true -> 1
            false -> 0
        }
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op8(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        // is-equal-to
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out: Long = when (a == b) {
            true -> 1
            false -> 0
        }
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op9(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        val a = read(data, pc+1, modes[0])
        rb += a
        return pc+2
    }

    suspend fun op99(data: MutableMap<Long, Long>, pc: Long, modes: List<ModeType>): Long {
        throw HaltException()
    }

    class HaltException : Throwable() {}
    class InvalidModeException : Throwable() {}

    suspend fun run() {
        try {
            while (true) {
                step()
            }
        } catch (e: HaltException) {
            //println("CPU HALT")
        }
        //println(program)
    }

    suspend fun step() {
        val opWithMode: Long = programMemory[pc]!!
        val op = opWithMode % 100
        val m0 = ModeType.from(((opWithMode / 100) % 10).toInt())
        val m1 = ModeType.from(((opWithMode / 1000) % 10).toInt())
        val m2 = ModeType.from(((opWithMode / 10000) % 10).toInt())
        val modes = listOf(m0, m1, m2)
        val opcode = opcodes[op] ?: error("Invalid opcode: $op")
        //println("pc: $pc  op: $op")
        pc = opcode.call(programMemory, pc, modes)
    }

    fun read(data: MutableMap<Long, Long>, value: Long, mode: ModeType = ModeType.INDIRECT): Long {
        //val a = data.getOrDefault(value, 0)  // hmm, this is part of the instruction, maybe not give default
        val a = data[value]!!
        return when (mode) {
            ModeType.INDIRECT -> data.getOrDefault(a, 0)
            ModeType.LITERAL -> a
            ModeType.RELATIVE -> data.getOrDefault(a + rb, 0)
        }
    }

    fun write(data: MutableMap<Long, Long>, location: Long, value: Long, mode: ModeType = ModeType.INDIRECT) {
        //val indirectLocation = data.getOrDefault(location, 0) // hmm, this is part of the instruction, maybe not give default
        val indirectLocation = data[location]!!
        when (mode) {
            ModeType.INDIRECT -> data[indirectLocation] = value
            ModeType.LITERAL -> data[indirectLocation] = value
            ModeType.RELATIVE -> data[indirectLocation + rb] = value
        }
    }
}

class Day9: IDay {
    override val part1InputFilename: String = "9.txt"
    override val part2InputFilename: String = "9.txt"


    fun parseInput(raw_input: String): List<Long> {
        return raw_input.trim().split(",").map { it.trim().toLong() }
    }

    override fun runPart1(raw_input: String): Long {
        val program = parseInput(raw_input).toMutableList()
        //val program = parseInput("""109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99""").toMutableList()
        //val program = parseInput("""1102,34915192,34915192,7,4,7,99,0""").toMutableList()
        //val program: MutableList<Long> = parseInput("""104,1125899906842624,99""").toMutableList()
        val output = runBlocking {
            //sampleStart
            val N = 1
            //val channels = ArrayList<Channel<Int>>()
            val channels = (0 until N).map { Channel<Long>(UNLIMITED) }
            val cpus = (0 until N).map { CPU(program.toMutableList(), channels[it], channels[(it+1) % N]) }
            val phases = arrayListOf<Long>(1)

            for ((phase, cpu) in (phases zip cpus)) {
                cpu.inputDevice.feedInput(phase)
            }

            // cpu0 has initial input of 0
            //cpus[0].inputDevice.feedInput(1)

            val jobs = cpus.map { cpu ->
                async {
                    cpu.run()
                }
                //lastOutput = cpu.outputDevice.buffer.pop()
            }
            jobs.last().join()  // only care about last stage
            val output = cpus.last().outputDevice.buffer

            return@runBlocking output
        }
        println("Part 1 Output: $output")
        return output.last()
    }

    override fun runPart2(raw_input: String): Long {
        val program = parseInput(raw_input).toMutableList()
        //val program = parseInput("""109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99""").toMutableList()
        //val program = parseInput("""1102,34915192,34915192,7,4,7,99,0""").toMutableList()
        //val program: MutableList<Long> = parseInput("""104,1125899906842624,99""").toMutableList()
        val output = runBlocking {
            //sampleStart
            val N = 1
            //val channels = ArrayList<Channel<Int>>()
            val channels = (0 until N).map { Channel<Long>(UNLIMITED) }
            val cpus = (0 until N).map { CPU(program.toMutableList(), channels[it], channels[(it+1) % N]) }
            val phases = arrayListOf<Long>(2)

            for ((phase, cpu) in (phases zip cpus)) {
                cpu.inputDevice.feedInput(phase)
            }

            // cpu0 has initial input of 0
            //cpus[0].inputDevice.feedInput(1)

            val jobs = cpus.map { cpu ->
                async {
                    cpu.run()
                }
                //lastOutput = cpu.outputDevice.buffer.pop()
            }
            jobs.last().join()  // only care about last stage
            val output = cpus.last().outputDevice.buffer

            return@runBlocking output
        }
        println("Part 2 Output: $output")
        return output.last()
    }
}