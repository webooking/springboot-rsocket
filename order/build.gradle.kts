plugins {
    idea
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.spring") version "1.5.10"

    id("org.springframework.boot") version "2.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "org.study"
version = "1.0.0"

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}
repositories {
    mavenCentral()
    maven(url = "http://localhost:5433/repository/rsocket/")
}

dependencies {
    val kotestVersion = "4.4.3"
    val springmockkVersion = "3.0.1"
    val commonVersion = "1.0.0"

    implementation("org.study:feign:1.0.0")
    implementation("org.springframework.data:spring-data-commons:2.5.1")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.boot:spring-boot-starter-rsocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-messaging")
    implementation("org.springframework.security:spring-security-rsocket")
    implementation("org.springframework.security:spring-security-oauth2-resource-server"){
        exclude(module = "spring-security-web")
        exclude(module = "spring-web")
    }

    implementation("org.study:common:$commonVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring-jvm:$kotestVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.4.0")
}
tasks {
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "7.0.2"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
}