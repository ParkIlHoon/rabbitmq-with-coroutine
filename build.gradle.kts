import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"

    // OS Detector for Netty
    id("com.google.osdetector") version "1.7.1"
}

group = "dev.hoon"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Springboot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Webflux + Coroutine
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("io.projectreactor:reactor-test")

    // AMQP + RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    testImplementation("org.springframework.amqp:spring-rabbit-test")

    // Test Containers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:rabbitmq")

    /**
     * M1에서 빌드할 경우 아래 오류가 발생하는데, 아래 의존성을 추가해주면 해결이 된다.
     *
     * i.n.r.d.DnsServerAddressStreamProviders : Unable to load io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider, fallback to system defaults.
     * This may result in incorrect DNS resolutions on MacOS.
     * Check whether you have a dependency on 'io.netty:netty-resolver-dns-native-macos'.
     * Use DEBUG level to see the full stack: java.lang.UnsatisfiedLinkError: failed to load the required native library
     */
    if (osdetector.classifier == "osx-aarch_64") {
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.97.Final:${osdetector.classifier}")
    }

    // Spring Doc
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
