package org.study.feign

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.study.feign.model.FeignClientMapping as FeignClientMappingDto

class RSocketClientsRegistrar : ImportBeanDefinitionRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clients: Map<String, FeignClientMappingDto> = parseFeignClientMappings(importingClassMetadata)
        log.info("parseFeignClientMappings: $clients")
        if(!registry.containsBeanDefinition("rSocketClientBuilder")){
            log.info("has not a bean rSocketClientBuilder")
            registry.registerBeanDefinition("rSocketClientBuilder", RootBeanDefinition("org.study.feign.util.RSocketClientBuilder"))
        }
        log.info("Does have bean rSocketClientBuilder? ${registry.containsBeanDefinition("rSocketClientBuilder")}")
        /*clients.map {
            registry.registerBeanDefinition(it.key, beanDefinition(it.value))
        }*/
        super.registerBeanDefinitions(importingClassMetadata, registry)
    }

    /*private fun beanDefinition(value: FeignClientMappingDto): BeanDefinition {

    }*/

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