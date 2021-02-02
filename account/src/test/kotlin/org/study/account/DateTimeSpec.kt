package org.study.account

import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class DateTimeSpec:StringSpec({
    "datetime"{
        val utcZone = ZoneId.of("UTC")
        val beijingZone = ZoneId.of("Asia/Shanghai")
        val vancouverZone = ZoneId.of("America/Vancouver")

        TimeZone.setDefault(TimeZone.getTimeZone(utcZone))

        val now = LocalDateTime.now()
        val vancouver = now.atZone(utcZone).withZoneSameInstant(vancouverZone).format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z"))
        println(vancouver) //02/01/2021 - 22:46:29 -0800

        val beijing = now.atZone(utcZone).withZoneSameInstant(beijingZone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"))
        println(beijing) //2021-02-02 14:46:29 +0800
    }
})