package dev.lue.aoc19.Day5

import dev.lue.aoc19.IDay
import java.util.*

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

data class Opcode(val op: Int, val call: OpcodeFunction, val parameters: List<IOType>)


interface IReadDevice {
    fun read(): Int
}

interface IWriteDevice {
    fun write(value: Int)
}

interface IReadWriteDevice: IReadDevice, IWriteDevice

class IntcodeInput: IReadDevice {
    val buffer: Deque<Int> = LinkedList()

    override fun read(): Int {
        return buffer.pollFirst()
    }

    fun feedInput(value: Int) {
        buffer.addLast(value)
    }
}

class IntcodeOutput: IWriteDevice {
    val buffer: Deque<Int> = LinkedList()

    override fun write(value: Int) {
        buffer.addLast(value)
    }
}

class CPU constructor(val program: MutableList<Int>) {
    var pc: Int = 0
    val inputDevice = IntcodeInput()
    val outputDevice = IntcodeOutput()

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

    fun op1(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // add
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a + b
        //println("$a + $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    fun op2(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // mul
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a * b
        //println("$a * $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    fun op3(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // read from input, write to indirect location
        if (modes[0] != ModeType.INDIRECT) {
            throw InvalidModeException()
        }
        val a = inputDevice.read()
        //println("$a * $b = $out")
        write(data, pc+1, a, modes[0])
        return pc+2
    }

    fun op4(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // write to output
        val a = read(data, pc+1, modes[0])
        outputDevice.write(a)
        return pc+2
    }

    fun op5(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // jump-if-true
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0 -> pc + 3
            else -> read(data, pc+2, modes[1])
        }
    }

    fun op6(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        // jump-if-false
        val a = read(data, pc+1, modes[0])
        return when (a) {
            0 -> read(data, pc+2, modes[1])
            else -> pc + 3
        }
    }

    fun op7(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
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

    fun op8(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
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

    fun op99(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        throw HaltException()
    }

    class HaltException : Throwable() {}
    class InvalidModeException : Throwable() {}

    fun run() {
        try {
            while (true) {
                step()
            }
        } catch (e: HaltException) {
            //println("CPU HALT")
        }
        //println(program)
    }

    fun step() {
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

class Day5: IDay {
    override val part1InputFilename: String = "5.txt"
    override val part2InputFilename: String = "5.txt"


    fun parseInput(raw_input: String): List<Int> {
        return raw_input.split(",").map { it.toInt() }
    }

    override fun runPart1(raw_input: String): Int {
        val program = parseInput(raw_input).toMutableList()
        val cpu = CPU(program)
        cpu.inputDevice.feedInput(1)
        cpu.run()
        val output = cpu.outputDevice.buffer.toList()
        println("Part 1 Output: $output")
        return output.last()
    }

    override fun runPart2(raw_input: String): Int {
        val program = parseInput(raw_input).toMutableList()
        val cpu = CPU(program)
        cpu.inputDevice.feedInput(5)
        cpu.run()
        val output = cpu.outputDevice.buffer.toList()
        println("Part 2 Output: $output")
        return output.last()
    }
}