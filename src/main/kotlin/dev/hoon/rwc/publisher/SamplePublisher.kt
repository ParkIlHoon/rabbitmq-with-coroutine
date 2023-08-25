package dev.hoon.rwc.publisher

import dev.hoon.rwc.constants.RabbitMqConstants
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class SamplePublisher(
    private val rabbitTemplate: RabbitTemplate
) {
    suspend fun pulish(message: String) {
        rabbitTemplate.convertAndSend(RabbitMqConstants.EXCHANGE_DIRECT, RabbitMqConstants.SAMPLE_ROUTING_KEY, message)
    }
}