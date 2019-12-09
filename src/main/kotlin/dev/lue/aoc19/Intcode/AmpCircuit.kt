package dev.lue.aoc19.Intcode

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlin.math.max

class AmpCircuit {

    fun findMaxThruster(raw_program: String, phaseOptions: List<IntcodeInt>): IntcodeInt {
        val program = parseInput(raw_program).toMutableList()
        //val program = parseInput("""3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5""").toMutableList()
        var output = IntcodeInt.MIN_VALUE
        for (phases in getPermutations(phaseOptions)) {
            val ret = runAmpCircuit(program, phases)
            output = max(output, ret)
        }
        return output
    }

    fun runAmpCircuit(program: MutableList<IntcodeInt>, phases: List<IntcodeInt>): IntcodeInt {
        val output = runBlocking {
            val N = phases.size
            val channels = (0 until N).map { Channel<IntcodeInt>(Channel.UNLIMITED) }
            val cpus = (0 until N).map { Processor(program.toMutableList(), channels[it], channels[(it+1) % N]) }

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