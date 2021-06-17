package org.study.feign

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.study.feign.model.factory.RSocketRequesterBuilderFactoryBean
import org.study.feign.model.factory.RSocketRequesterFactoryBean
import org.study.feign.model.factory.RSocketStrategiesFactoryBean
import org.study.feign.util.RSocketClientBuilder
import org.study.feign.model.FeignClientMapping as FeignClientMappingDto

class RSocketClientsRegistrar : ImportBeanDefinitionRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clients: Map<String, FeignClientMappingDto> = parseFeignClientMappings(importingClassMetadata)
        log.info("parseFeignClientMappings: $clients")
        registry.registerBeanDefinition("rSocketStrategies", RootBeanDefinition(RSocketStrategiesFactoryBean::class.java))
        registry.registerBeanDefinition(
            "rSocketRequesterBuilder",
            RootBeanDefinition(RSocketRequesterBuilderFactoryBean::class.java, ConstructorArgumentValues().apply {
                addGenericArgumentValue((registry as DefaultListableBeanFactory).getBean(RSocketStrategies::class.java))
            }, null).apply {
                scope = ConfigurableBeanFactory.SCOPE_PROTOTYPE
            })
        clients.map {
            registry.registerBeanDefinition("${it.key}RSocketRequester", RootBeanDefinition(RSocketRequesterFactoryBean::class.java, ConstructorArgumentValues().apply {
                addGenericArgumentValue( (registry as DefaultListableBeanFactory).getBean(RSocketRequester.Builder::class.java))
                addGenericArgumentValue(it.value.host)
                addGenericArgumentValue(it.value.port)
            },null))
        }
        registry.registerBeanDefinition("rSocketClientBuilder", RootBeanDefinition(RSocketClientBuilder::class.java))
        super.registerBeanDefinitions(importingClassMetadata, registry)
    }


    @Suppress("UNCHECKED_CAST")
    private fun parseFeignClientMappings(importingClassMetadata: AnnotationMetadata): Map<String, FeignClientMappingDto> {
        val map = importingClassMetadata.getAnnotationAttributes(EnableRSocketClients::class.java.canonicalName)
        val values = map!!["value"] as Array<AnnotationAttributes>
        return values.associate {
            it.getString("name") to FeignClientMappingDto(
                it.getString("host"),
                it.getNumber("port"),
                it.getClassArray("classes")
            )
        }
    }
}