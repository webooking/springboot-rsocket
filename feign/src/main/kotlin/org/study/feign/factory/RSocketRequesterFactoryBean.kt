package org.study.feign.factory

import org.springframework.beans.factory.FactoryBean
import org.springframework.messaging.rsocket.RSocketRequester

class RSocketRequesterFactoryBean(
    private val builder: RSocketRequester.Builder,
    private val host: String,
    private val port: Int
) : FactoryBean<RSocketRequester> {
    override fun getObject(): RSocketRequester = builder.tcp(host, port)

    override fun getObjectType(): Class<*> = RSocketRequester::class.java
}