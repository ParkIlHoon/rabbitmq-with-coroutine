package dev.hoon.rwc.consumer

import dev.hoon.rwc.constants.RabbitMqConstants
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Service
class SampleConsumer {
    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(value = RabbitMqConstants.SAMPLE_QUEUE_NAME),
            exchange = Exchange(value = RabbitMqConstants.EXCHANGE_DIRECT),
            key = [RabbitMqConstants.SAMPLE_ROUTING_KEY]
        )]
    )
    suspend fun sampleListener(@Payload payload: String): Mono<Unit> {
        logger.info { "message received : $payload" }
        return Mono.empty()
    }
}