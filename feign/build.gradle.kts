plugins {
    kotlin("jvm") version "1.5.10"
    idea
    `maven-publish`
}

group = "org.study"
version = "1.0.1"

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    api("org.springframework:spring-context:5.2.7.RELEASE")
    api("org.slf4j:slf4j-api:1.7.30")
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
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            isAllowInsecureProtocol = true
            setUrl("http://localhost:5433/repository/rsocket")
            credentials {
                username = "admin"
                password = "Yurilee1986."
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