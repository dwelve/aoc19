package dev.lue.aoc19

class Day1 : IDay {
    override val part1InputFilename: String = "1.txt"
    override val part2InputFilename: String = "1.txt"

    fun getFuel(mass: Int): Int {
        return (mass / 3) - 2
    }

    fun getFuel2(mass: Int, acc: Int = 0): Int {
        val fuel = getFuel(mass)
        return if (fuel > 0) getFuel2(fuel, acc + fuel) else acc
    }

    fun parseInput(raw_input: String): List<Int> {
        return raw_input.split("\n").map { it.toInt() }
    }

    override fun runPart1(raw_input: String): Int {
        val masses = parseInput(raw_input)
        val ans = masses.map{ getFuel(it) }.sum()
        return ans
    }

    /*
    def get_fuel2(mass, acc=0):
        fuel = get_fuel(mass)
        if fuel > 0:
            return get_fuel2(fuel, acc+fuel)
        else:
            return acc
    */
    override fun runPart2(raw_input: String): Int {
        val masses = parseInput(raw_input)
        val ans = masses.map{ getFuel2(it) }.sum()
        return ans
    }
}