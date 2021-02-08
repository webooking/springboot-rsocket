@file:Suppress("SpellCheckingInspection")

plugins {
    idea
    `maven-publish`
    kotlin("jvm") version "1.4.21"
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

group = "org.study"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.4")
    implementation("io.rsocket:rsocket-core:1.1.0")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.springframework:spring-messaging:5.3.3")
    implementation("jakarta.validation:jakarta.validation-api:2.0.2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.8.1"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            setUrl("http://localhost:5433/repository/rsocket")
            credentials {
                username = "admin"
                password = "123456"
            }
        }
    }
    publications {
        register("mavenKotlin", MavenPublication::class) {
            from(components["kotlin"])
            artifact(sourcesJar.get())
        }
    }
}