package org.study.feign

import org.slf4j.LoggerFactory
import org.study.feign.util.RSocketClientBuilder

class RSocketClientsRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)
    constructor(rsocketClientBuilder: RSocketClientBuilder){
        log.info("build class RSocketClientsRegistrar, $rsocketClientBuilder")
    }
}