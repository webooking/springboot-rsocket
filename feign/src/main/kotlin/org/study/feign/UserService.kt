package org.study.feign

interface UserService {
    fun sayHello(name: String): String
    fun others(): Unit
}
