package dev.lue.aoc19.Intcode

import kotlinx.coroutines.channels.Channel
import java.util.*


interface IReadDevice<T> {
    suspend fun read(): T
}

interface IWriteDevice<T> {
    suspend fun write(value: T)
}

interface IReadWriteDevice<T>: IReadDevice<T>, IWriteDevice<T>

class ChannelReader<T>(val channel: Channel<T>): IReadDevice<T> {
    val buffer: Deque<T> = LinkedList()

    override suspend fun read(): T {
        val value = channel.receive()
        buffer.addLast(value)
        return value
    }

    suspend fun feedInput(value: T) {
        channel.send(value)
    }
}

class ChannelWriter<T>(val channel: Channel<T>): IWriteDevice<T> {
    val buffer: Deque<T> = LinkedList()

    override suspend fun write(value: T) {
        buffer.addLast(value)
        channel.send(value)
    }
}
