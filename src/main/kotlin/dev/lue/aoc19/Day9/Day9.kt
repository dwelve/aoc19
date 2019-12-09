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

typealias OpcodeFunction = (MutableMap<Int, Int>, Int, List<ModeType>) -> Int
//typealias IOFunction = (MutableList<Int>, Int, ModeType) -> ()

data class Opcode(
    val op: Int, val call: KSuspendFunction3<
            @ParameterName(name = "data") MutableMap<Int, Int>,
            @ParameterName(name = "pc") Int, @ParameterName(name = "modes") List<ModeType>, Int>,
            val parameters: List<IOType>)


interface IReadDevice {
    suspend fun read(): Int
}

interface IWriteDevice {
    suspend fun write(value: Int)
}

interface IReadWriteDevice: IReadDevice, IWriteDevice

class IntcodeInput(val channel: Channel<Int>): IReadDevice {
    val buffer: Deque<Int> = LinkedList()

    override suspend fun read(): Int {
        val value = channel.receive()
        buffer.addLast(value)
        return value
    }

    suspend fun feedInput(value: Int) {
        channel.send(value)
    }
}

class IntcodeOutput(val channel: Channel<Int>): IWriteDevice {
    val buffer: Deque<Int> = LinkedList()

    override suspend fun write(value: Int) {
        buffer.addLast(value)
        channel.send(value)
    }
}

class CPU constructor(val _program: MutableList<Long>, val inputChannel: Channel<Long>, val outputChannel: Channel<Long>) {
    val programMemory = _program.mapIndexed { index, value -> index to value }.toMap().toMutableMap()
    var pc: Int = 0  // program counter
    var rb: Int = 0  // relative base
    val inputDevice = IntcodeInput(inputChannel)
    val outputDevice = IntcodeOutput(outputChannel)

    val opcodes: Map<Int, Opcode > = mapOf(
        1 to Opcode(1, ::op1, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        2 to Opcode(2, ::op2, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        3 to Opcode(3, ::op3, listOf(IOType.READ, IOType.WRITE)),
        4 to Opcode(4, ::op4, listOf(IOType.READ)),
        5 to Opcode(5, ::op5, listOf(IOType.READ, IOType.READ)),
        6 to Opcode(6, ::op6, listOf(IOType.READ, IOType.READ)),
        7 to Opcode(7, ::op7, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        8 to Opcode(8, ::op8, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        9 to Opcode(9, ::op9, listOf(IOType.READ)),

        99 to Opcode(99, ::op99, emptyList())
    )

    suspend fun op1(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // add
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a + b
        //println("$a + $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op2(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // mul
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a * b
        //println("$a * $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op3(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // read from input, write to indirect location
        if (modes[0] != ModeType.INDIRECT) {
            throw InvalidModeException()
        }
        val a = inputDevice.read()
        //println("$a * $b = $out")
        write(data, pc+1, a, modes[0])
        return pc+2
    }

    suspend fun op4(data:MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // write to output
        val a = read(data, pc+1, modes[0])
        outputDevice.write(a)
        return pc+2
    }

    suspend fun op5(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // jump-if-true
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0 -> pc + 3
            else -> read(data, pc+2, modes[1])
        }
    }

    suspend fun op6(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // jump-if-false
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0 -> read(data, pc+2, modes[1])
            else -> pc + 3
        }
    }

    suspend fun op7(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // is-less-than
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = when (a < b) {
            true -> 1
            false -> 0
        }
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op8(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        // is-equal-to
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = when (a == b) {
            true -> 1
            false -> 0
        }
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op9(data: MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
        val a = read(data, pc+1, modes[0])
        rb += a
        return pc+2
    }

    suspend fun op99(data:MutableMap<Int, Int>, pc: Int, modes: List<ModeType>): Int {
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
        val opWithMode = programMemory[pc]!!
        val op = opWithMode % 100
        val m0 = ModeType.from((opWithMode / 100) % 10)
        val m1 = ModeType.from((opWithMode / 1000) % 10)
        val m2 = ModeType.from((opWithMode / 10000) % 10)
        val modes = listOf(m0, m1, m2)
        val opcode = opcodes[op] ?: error("Invalid opcode: $op")
        //println("pc: $pc  op: $op")
        pc = opcode.call(programMemory, pc, modes)
    }

    fun read(data: MutableMap<Int, Int>, value: Int, mode: ModeType = ModeType.INDIRECT): Int {
        //val a = data.getOrDefault(value, 0)  // hmm, this is part of the instruction, maybe not give default
        val a = data[value]!!
        return when (mode) {
            ModeType.INDIRECT -> data.getOrDefault(a, 0)
            ModeType.LITERAL -> a
            ModeType.RELATIVE -> data.getOrDefault(a + rb, 0)
        }
    }

    fun write(data: MutableMap<Int, Int>, location: Int, value: Int, mode: ModeType = ModeType.INDIRECT) {
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
        //val program = parseInput(raw_input).toMutableList()
        //val program = parseInput("""109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99""").toMutableList()
        //val program = parseInput("""1102,34915192,34915192,7,4,7,99,0""").toMutableList()
        val program = parseInput("""104,1125899906842624,99""").toMutableList()
        val output = runBlocking {
            //sampleStart
            val N = 1
            //val channels = ArrayList<Channel<Int>>()
            val channels = (0 until N).map { Channel<Int>(UNLIMITED) }
            val cpus = (0 until N).map { CPU(program.toMutableList(), channels[it], channels[(it+1) % N]) }
            val phases = arrayListOf(1)

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

    override fun runPart2(raw_input: String): Int {
        //val program = parseInput(raw_input).toMutableList()
        val program = parseInput("""3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,
27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5""").toMutableList()
        val output = runBlocking {
            //sampleStart
            val N = 5
            //val channels = ArrayList<Channel<Int>>()
            val channels = (0 until N).map { Channel<Int>(UNLIMITED) }
            val cpus = (0 until N).map { CPU(program.toMutableList(), channels[it], channels[(it+1) % N]) }
            val phases = arrayListOf(9,8,7,6,5)

            for ((phase, cpu) in (phases zip cpus)) {
                cpu.inputDevice.feedInput(phase)
            }

            // cpu0 has initial input of 0
            cpus[0].inputDevice.feedInput(0)

            val jobs = cpus.map { cpu ->
                async {
                    cpu.run()
                }
                //lastOutput = cpu.outputDevice.buffer.pop()
            }
            jobs.last().join()  // only care about last stage
            val output = cpus.last().outputDevice.buffer.last()

            return@runBlocking output
        }
        println("Part 2 Output: $output")
        return output
    }

    fun runAmpCircuit(program: MutableList<Int>, phases: List<Int>): Int {
        val output = runBlocking {
            val N = phases.size
            val channels = (0 until N).map { Channel<Int>(UNLIMITED) }
            val cpus = (0 until N).map { CPU(program.toMutableList(), channels[it], channels[(it+1) % N]) }

            for ((phase, cpu) in (phases zip cpus)) {
                cpu.inputDevice.feedInput(phase)
            }

            // cpu0 has initial input of 0
            cpus[0].inputDevice.feedInput(0)

            val jobs = cpus.map { cpu ->
                async {
                    cpu.run()
                }
                //lastOutput = cpu.outputDevice.buffer.pop()
            }
            jobs.last().join()  // only care about last stage
            val output = cpus.last().outputDevice.buffer.last()

            return@runBlocking output
        }
        return output
    }
}