package dev.lue.aoc19.Day5

import dev.lue.aoc19.IDay

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

class CPU constructor(val program: MutableList<Int>) {
    var pc: Int = 0
    val opcodes: Map<Int, Opcode > = mapOf(
        1 to Opcode(1, ::op1, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        2 to Opcode(2, ::op2, listOf(IOType.READ, IOType.READ, IOType.WRITE)),
        99 to Opcode(99, ::op99, emptyList())
    )

    fun op1(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a + b
        //println("$a + $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    fun op2(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        val a = read(data, pc+1, modes[0])
        val b = read(data, pc+2, modes[1])
        val out = a * b
        //println("$a * $b = $out")
        write(data, pc+3, out, modes[2])
        return pc+4
    }

    fun op99(data: MutableList<Int>, pc: Int, modes: List<ModeType>): Int {
        throw HaltException()
    }

    class HaltException : Throwable() {

    }

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
        program[1] = 12
        program[2] = 2

        val cpu = CPU(program)
        cpu.run()
        return program[0]
    }

    override fun runPart2(raw_input: String): Int {
        val goal: Int = 19690720
        for (noun in 0..99) {
            for (verb in 0..99) {
                val program = parseInput(raw_input).toMutableList()
                program[1] = noun
                program[2] = verb
                val cpu = CPU(program)
                cpu.run()
                if (program[0] == goal) {
                    return 100*noun + verb
                }
            }
        }
        return 0
    }
}