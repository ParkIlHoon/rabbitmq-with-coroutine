# Coroutine으로 개발하는 RabbitMQ Consumer와 Publisher


## 비동기 @RabbitListener
`@RabbitListener` 혹은 `@RabbitHandler` 메서드는 `CompletableFuture<?>`나 `Mono<?>`와 같은 비동기 반환 타입을 지정할 수 있다.

```kotlin
@RabbitListener(...)
suspend fun sampleListener(@Payload payload: String): Mono<Unit> {
    ...
}
```

이 때 `@RabbitListener` 혹은 `@RabbitHandler` 메서드의 반환 타입을 비동기 반환 타입으로 지정하더라도 아래와 같은 오류가 발생한다.

```
2023-08-26T17:46:00.089+09:00 ERROR 15825 --- [ 127.0.0.1:5672] o.s.a.r.c.CachingConnectionFactory       : Shutdown Signal: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - unknown delivery tag 1, class-id=60, method-id=80)
2023-08-26T17:46:00.090+09:00  WARN 15825 --- [ntContainer#0-4] .a.r.l.a.MessagingMessageListenerAdapter : Container AcknowledgeMode must be MANUAL for a Mono<?> return type(or Kotlin suspend function); otherwise the container will ack the message immediately
```

오류의 이유는 [Spring AMQP 공식 문서](https://docs.spring.io/spring-amqp/docs/current/reference/html/#async-returns)에서 확인할 수 있는데, 그 내용은 아래와 같다.

> `ListenerContainerFactory`는 컨슈머 쓰레드가 메시지를 `ack`하지 않고, 비동기 작업이 완료될 때 메시지를 `ack`하거나 `nack` 하기 위해 반드시 AckMode를 `AcknowledgeMode.MANUAL`로 지정해야한다.
> 비동기 작업 중 에러가 발생할 경우 예외 타입, 컨테이너 구성, 컨테이너 예외 핸들러에 따라 메시지 재큐잉 여부가 결정된다.


그리고 이 오류가 계속해서 발생하면서 결국에는 아래와 같은 예외를 마주하게 된다.

```
2023-08-26T22:33:22.758+09:00 ERROR 17710 --- [atcher-worker-9] reactor.core.publisher.Operators         : Operator called default onErrorDropped

java.lang.IllegalStateException: Channel closed; cannot ack/nack
	at org.springframework.amqp.rabbit.connection.CachingConnectionFactory$CachedChannelInvocationHandler.invoke(CachingConnectionFactory.java:1128) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at jdk.proxy2/jdk.proxy2.$Proxy121.basicAck(Unknown Source) ~[na:na]
	at org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener.basicAck(AbstractAdaptableMessageListener.java:443) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener.lambda$handleResult$3(AbstractAdaptableMessageListener.java:400) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at reactor.core.publisher.LambdaMonoSubscriber.onComplete(LambdaMonoSubscriber.java:135) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.Operators$MultiSubscriptionSubscriber.onComplete(Operators.java:2205) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.FluxFilter$FilterSubscriber.onComplete(FluxFilter.java:166) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.MonoCreate$DefaultMonoSink.success(MonoCreate.java:173) ~[reactor-core-3.5.8.jar:3.5.8]
	at kotlinx.coroutines.reactor.MonoCoroutine.onCompleted(Mono.kt:101) ~[kotlinx-coroutines-reactor-1.6.4.jar:na]
	at kotlinx.coroutines.AbstractCoroutine.onCompletionInternal(AbstractCoroutine.kt:93) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.JobSupport.tryFinalizeSimpleState(JobSupport.kt:294) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.JobSupport.tryMakeCompleting(JobSupport.kt:856) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.JobSupport.makeCompletingOnce$kotlinx_coroutines_core(JobSupport.kt:828) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.AbstractCoroutine.resumeWith(AbstractCoroutine.kt:100) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:46) ~[kotlin-stdlib-1.8.22.jar:1.8.22-release-407(1.8.22)]
	at kotlinx.coroutines.internal.DispatchedContinuationKt.resumeCancellableWith(DispatchedContinuation.kt:367) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable(Cancellable.kt:30) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable$default(Cancellable.kt:25) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.CoroutineStart.invoke(CoroutineStart.kt:110) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.AbstractCoroutine.start(AbstractCoroutine.kt:126) ~[kotlinx-coroutines-core-jvm-1.6.4.jar:na]
	at kotlinx.coroutines.reactor.MonoKt.monoInternal$lambda-2(Mono.kt:90) ~[kotlinx-coroutines-reactor-1.6.4.jar:na]
	at reactor.core.publisher.MonoCreate.subscribe(MonoCreate.java:58) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.Mono.subscribe(Mono.java:4495) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.Mono.subscribeWith(Mono.java:4561) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.Mono.subscribe(Mono.java:4462) ~[reactor-core-3.5.8.jar:3.5.8]
	at reactor.core.publisher.Mono.subscribe(Mono.java:4398) ~[reactor-core-3.5.8.jar:3.5.8]
	at org.springframework.amqp.rabbit.listener.adapter.MonoHandler.subscribe(MonoHandler.java:45) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener.handleResult(AbstractAdaptableMessageListener.java:397) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.invokeHandlerAndProcessResult(MessagingMessageListenerAdapter.java:226) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:149) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1660) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.actualInvokeListener(AbstractMessageListenerContainer.java:1579) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.invokeListener(AbstractMessageListenerContainer.java:1567) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doExecuteListener(AbstractMessageListenerContainer.java:1558) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListenerAndHandleException(AbstractMessageListenerContainer.java:1503) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.lambda$executeListener$8(AbstractMessageListenerContainer.java:1481) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at io.micrometer.observation.Observation.lambda$observe$0(Observation.java:493) ~[micrometer-observation-1.11.2.jar:1.11.2]
	at io.micrometer.observation.Observation.observeWithContext(Observation.java:603) ~[micrometer-observation-1.11.2.jar:1.11.2]
	at io.micrometer.observation.Observation.observe(Observation.java:492) ~[micrometer-observation-1.11.2.jar:1.11.2]
	at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.executeListener(AbstractMessageListenerContainer.java:1481) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.doReceiveAndExecute(SimpleMessageListenerContainer.java:994) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer.receiveAndExecute(SimpleMessageListenerContainer.java:941) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.mainLoop(SimpleMessageListenerContainer.java:1323) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:1225) ~[spring-rabbit-3.0.6.jar:3.0.6]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]
```

RabbitMQ에 메시지를 `ack` 혹은 `nack`할 수 없다는 오류인데, `Channel`이 닫혔기 때문이다.<br>
앞서 발생했던 오류를 자세히 보면 `reply-code=406, reply-text=PRECONDITION_FAILED` 라는 내용을 찾을 수 있는데, 이때 `Channel`에 Shutdown Signal이 전달되면서 닫힌 것이다.

특이한 것은 **오류가 발생한 뒤에는 같은 스레드에서 메시지를 구독하지 못하게 되는 것**이었다.<br>
예를 들어, `dispatcher-worker-1` 스레드에서 위 오류가 발생하면 해당 스레드에서는 다시 메시지를 구독하지 못했고, 새로운 스레드(`dispatcher-worker-*`)를 통해 메시지를 구독했다.
때문에 이 오류를 방치한다면 **결국 모든 스레드에서 메시지를 구독하지 못하게 될 것**이다.

그러면 이렇게 오류가 발생한 뒤에는 같은 스레드에서 메시지를 구독하지 못하게 되는 것일까?

`com.rabbitmq.client.Channel`의 API Document를 보면 아래 내용을 찾을 수 있다.
> ## Concurrency Considerations
> Channel instances must not be shared between threads. Applications should prefer using a Channel per thread instead of sharing the same Channel across multiple threads. While some operations on channels are safe to invoke concurrently, some are not and will result in incorrect frame interleaving on the wire. Sharing channels between threads will also interfere with Publisher Confirms . As such, applications need to use a Channel per thread.

이로써 `Channel`은 스레드당 하나만 생성이 된다는 것을 알 수 있다.

그리고 RabbitMQ의 [Concurrency 문서](https://www.rabbitmq.com/api-guide.html#concurrency)를 보면 아래 내용을 찾을 수 있다.
> It is possible to use channel pooling to avoid concurrent publishing on a shared channel: once a thread is done working with a channel, it returns it to the pool, making the channel available for another thread.

스레드가 작업이 완료되면 `Channel`을 Pool에 반납해 다른 스레드에서 해당 `Channel`을 사용할 수 있게끔 한다.

그렇다면 다음과 같은 결론이 도출된다.

1. A 스레드에서 `PRECONDITION_FAILED` 오류가 발생해 `Channel`에 Shutdown Signal이 전달되어 닫힘
2. 닫힌 `Channel`을 Pool에 반납
3. B 스레드에서 Pool에서 닫힌 `Channel`을 참조해 `java.lang.IllegalStateException: Channel closed; cannot ack/nack` 예외 발생
4. C 스레드에서 새로 `Channel`을 생성해 사용함