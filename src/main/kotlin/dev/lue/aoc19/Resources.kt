package dev.lue.aoc19

internal object Resources {
    fun get(fileName: String): String {
        return Resources.javaClass.classLoader.getResource(fileName).readText(Charsets.UTF_8)
    }
}