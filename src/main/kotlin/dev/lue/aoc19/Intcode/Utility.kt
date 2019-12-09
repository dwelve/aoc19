package dev.lue.aoc19.Intcode


fun parseInput(raw_input: String): List<IntcodeInt> {
    return raw_input.trim().split(",").map { it.trim().toLong() }
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