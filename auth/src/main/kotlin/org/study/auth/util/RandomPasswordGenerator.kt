package org.study.auth.util

import java.security.SecureRandom
import java.util.Collections


object RandomPasswordGenerator {
    @Suppress("SpellCheckingInspection")
    private const val template = "!#%&()*+{|}~./:;<=>?@[]^_`012346789ABCDEFGHIJKLMNOPQRSTVWXYZabcdefghijklmnopqrstuvwxyz"
    private val random = SecureRandom()

    fun generate(): String {
        val list = generateRandomTemplate()
        return List(23) {
            getRandomChar(list)
        }.joinToString(separator = "")
    }

    private fun generateRandomTemplate(): List<Char> {
        val list = template.toList()
        Collections.rotate(list, random.nextInt(list.size / 3))
        return list
    }

    private fun getRandomChar(list: List<Char>) = list[random.nextInt(list.size)]
}