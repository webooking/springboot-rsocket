package org.study.feign.model.factory

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies

class RSocketRequesterBuilderFactoryBean(private val strategies: RSocketStrategies) : FactoryBean<RSocketRequester.Builder> {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getObject(): RSocketRequester.Builder{
        val bean = RSocketRequester.builder().rsocketStrategies(strategies)
        log.info("create RSocketRequester.Builder: $bean")
        return bean
    }

    override fun getObjectType(): Class<*> = RSocketRequester.Builder::class.java

    override fun isSingleton(): Boolean = false
}