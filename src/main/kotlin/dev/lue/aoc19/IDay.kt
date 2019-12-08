package dev.lue.aoc19

interface IDay {
    val part1InputFilename: String
    val part2InputFilename: String

    fun runPart1(raw_input: String): Int
    fun runPart2(raw_input: String): Int

    fun run() {
        val part1Input = Resources.get(part1InputFilename)
        val ans1 = runPart1(part1Input)
        println("Part 1 answer is:\n$ans1")
        println()
        val part2Input = Resources.get(part2InputFilename)
        val ans2 = runPart2(part2Input)
        println("Part 2 answer is:\n$ans2")
    }

    companion object {
        fun newDay(dayNumber: Int): IDay {
            return when (dayNumber) {
                1 -> Day1()
                2 -> Day2()
                else -> throw IllegalStateException("Unknown day number")
            }
        }
    }
}