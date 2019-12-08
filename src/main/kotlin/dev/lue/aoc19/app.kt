package dev.lue.aoc19

fun main(args: Array<String>) {
    val day: Int = if (args.isEmpty()) 1 else args[0].toInt()
    println("Day $day")
    val app = IDay.newDay(day)


    app.run()
}