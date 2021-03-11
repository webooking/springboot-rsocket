package org.study.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DateTimeSpec:StringSpec({
    "duration"{
        val start = LocalDateTime.now()
        val end = start.plusSeconds(2)

        val d1 = Duration.ofSeconds(start.until(end, ChronoUnit.SECONDS))
        d1.seconds.shouldBe(2)

        val d2 = Duration.ofSeconds(end.until(start, ChronoUnit.SECONDS))
        d2.seconds.shouldBe(-2)
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}