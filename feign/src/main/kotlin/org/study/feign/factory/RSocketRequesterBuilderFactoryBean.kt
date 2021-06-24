package org.study.feign.factory

import org.springframework.beans.factory.FactoryBean
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies

class RSocketRequesterBuilderFactoryBean(private val strategies: RSocketStrategies) : FactoryBean<RSocketRequester.Builder> {

    override fun getObject(): RSocketRequester.Builder = RSocketRequester.builder().rsocketStrategies(strategies)

    override fun getObjectType(): Class<*> = RSocketRequester.Builder::class.java

    override fun isSingleton(): Boolean = false
}