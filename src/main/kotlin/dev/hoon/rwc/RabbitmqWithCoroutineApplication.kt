package dev.hoon.rwc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbitmqWithCoroutineApplication

fun main(args: Array<String>) {
    runApplication<RabbitmqWithCoroutineApplication>(*args)
}
