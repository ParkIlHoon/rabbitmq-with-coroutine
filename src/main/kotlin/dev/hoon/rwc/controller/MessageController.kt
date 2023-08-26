package dev.hoon.rwc.controller

import dev.hoon.rwc.controller.dto.MessagePublishDto
import dev.hoon.rwc.publisher.SamplePublisher
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "메시지 발행 API")
@RestController
@RequestMapping("/api/v1/messages")
class MessageController(
    private val samplePublisher: SamplePublisher
) {
    @PostMapping
    suspend fun publish(@RequestBody messagePublishDtos: List<MessagePublishDto>) {
        messagePublishDtos.forEach {
            samplePublisher.pulish(it)
        }
    }
}