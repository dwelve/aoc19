package dev.lue.aoc19

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class Day1Test{
    @ParameterizedTest(name = "getFuel({0}) = {1}")
    @CsvSource(
        "12, 2",
        "14, 2",
        "1969, 654",
        "100756, 33583"
    )
    fun getFuelTest(mass: Int, expectedResult: Int) {
        val app = Day1()
        assertEquals(expectedResult, app.getFuel(mass), "getFuel($mass) should equal $expectedResult")
    }

    @ParameterizedTest(name = "getFuel2({0}) = {1}")
    @CsvSource(
        "100756, 50346"
    )
    fun getFuel2Test(mass: Int, expectedResult: Int) {
        val app = Day1()
        assertEquals(expectedResult, app.getFuel2(mass), "getFuel($mass) should equal $expectedResult")
    }

    @Test
    fun solutionTest() {
        val app = Day1()
        app.run()
    }
}