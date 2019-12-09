package dev.lue.aoc19.Day7

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
    LITERAL(1);

    companion object {
        fun from(findMode: Int): ModeType = ModeType.values().first { it.mode == findMode }
    }
}

typealias OpcodeFunction = (MutableList<Int>, Int, List<ModeType>) -> Int
//typealias IOFunction = (MutableList<Int>, Int, ModeType) -> ()

data class Opcode(
    val op: Int, val call: KSuspendFunction3<@ParameterName(name = "data") MutableList<Int>, @ParameterName(
        name = "pc"
    ) Int, @ParameterName(name = "modes") List<ModeType>, Int>, val parameters: List<IOType>)


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

class CPU constructor(val program: MutableList<Int>, val inputChannel: Channel<Int>, val outputChannel: Channel<Int>) {
    var pc: Int = 0
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

        99 to Opcode(99, ::op99, emptyList())
    )

    suspend fun op1(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // add
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a + b
        //println("$a + $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op2(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // mul
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a * b
        //println("$a * $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    suspend fun op3(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // read from input, write to indirect location
        if (modes[0] != ModeType.INDIRECT) {
            throw InvalidModeException()
        }
        val a = inputDevice.read()
        //println("$a * $b = $out")
        write(data, pc+1, a, modes[0])
        return pc+2
    }

    suspend fun op4(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // write to output
        val a = read(data, pc+1, modes[0])
        outputDevice.write(a)
        return pc+2
    }

    suspend fun op5(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // jump-if-true
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0 -> pc + 3
            else -> read(data, pc+2, modes[1])
        }
    }

    suspend fun op6(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // jump-if-false
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0 -> read(data, pc+2, modes[1])
            else -> pc + 3
        }
    }

    suspend fun op7(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
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

    suspend fun op8(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
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

    suspend fun op99(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
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
        val opWithMode = program[pc]
        val op = opWithMode % 100
        val m0 = ModeType.from((opWithMode / 100) % 10)
        val m1 = ModeType.from((opWithMode / 1000) % 10)
        val m2 = ModeType.from((opWithMode / 10000) % 10)
        val modes = listOf(m0, m1, m2)
        val opcode = opcodes[op] ?: error("Invalid opcode: $op")
        //println("pc: $pc  op: $op")
        pc = opcode.call(program, pc, modes)
    }

    fun read(data: MutableList<Int>, value: Int, mode: ModeType = ModeType.INDIRECT): Int {
        val a = data[value]
        return when (mode) {
            ModeType.INDIRECT -> data[a]
            ModeType.LITERAL -> a
        }
    }

    fun write(data: MutableList<Int>, location: Int, value: Int, mode: ModeType = ModeType.INDIRECT) {
        // ignore mode bit
        val indirectLocation = data[location]
        data[indirectLocation] = value
        /*when (mode) {
            ModeType.INDIRECT -> data[location] = value
            ModeType.LITERAL -> data[location] = value
        }*/
    }
}

class Day7: IDay {
    override val part1InputFilename: String = "7.txt"
    override val part2InputFilename: String = "7.txt"


    fun parseInput(raw_input: String): List<Int> {
        return raw_input.trim().split(",").map { it.trim().toInt() }
    }

    override fun runPart1(raw_input: String): Int {
        //val program = parseInput(raw_input).toMutableList()
        val program = parseInput("""3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0""").toMutableList()
        val output = runBlocking {
            //sampleStart
            val N = 5
            //val channels = ArrayList<Channel<Int>>()
            val channels = (0 until N).map { Channel<Int>(UNLIMITED) }
            val cpus = (0 until N).map { CPU(program.toMutableList(), channels[it], channels[(it+1) % N]) }
            val phases = arrayListOf(4, 3, 2, 1, 0)

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
        println("Part 1 Output: $output")
        return output
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

fun <T> getPermutations(items: List<T>, acc: List<List<T>> = listOf(emptyList())) : List<List<T>> {
    if (items.isEmpty()) {
        return acc
    }

    val allPerms = mutableListOf<List<T>>()
    for (i in 0 until items.size) {
        val addItem = items[i]
        val newItems = items.toMutableList()
        newItems.removeAt(i)
        val nextAcc = mutableListOf<List<T>>()
        for (permAcc in acc) {
            val nextPermAcc = permAcc.plus(addItem)
            nextAcc.add(nextPermAcc)
        }
        val newPerms = getPermutations(newItems, nextAcc)
        allPerms.addAll(newPerms)
    }

    return allPerms
}