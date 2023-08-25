package dev.hoon.rwc

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestRabbitmqWithCoroutineApplication {

    @Bean
    @ServiceConnection
    fun rabbitContainer(): RabbitMQContainer {
        return RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"))
    }

}

fun main(args: Array<String>) {
    fromApplication<RabbitmqWithCoroutineApplication>().with(TestRabbitmqWithCoroutineApplication::class).run(*args)
}
