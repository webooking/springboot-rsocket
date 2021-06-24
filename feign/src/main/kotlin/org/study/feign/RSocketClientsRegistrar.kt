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
import org.study.feign.factory.*

class RSocketClientsRegistrar : ImportBeanDefinitionRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clients: Map<String, FeignClientMappingDto> = parseFeignClientMappings(importingClassMetadata)
        registerRSocketStrategies(registry)
        dependsOnRSocketStrategies(registry)
        registerFeignClientBuilder(registry)

        clients.map { client ->
            val requesterName = "${client.key}RSocketRequester"
            registerRSocketRequester(registry, requesterName, client)

            client.value.classes.forEach { clazz ->
                registerFeignClient(
                    registry,
                    requesterName,
                    "${client.key}${clazz.simpleName.replaceFirstChar { it.uppercase() }}FeignClient",
                    clazz
                )
            }
        }
        log.info("Successfully initialized rsocket-client-registrar")
    }

    private fun registerFeignClient(
        registry: BeanDefinitionRegistry,
        requesterName: String,
        beanName: String,
        clazz: Class<*>
    ) {
        val requester = (registry as DefaultListableBeanFactory).getBean(requesterName)
        val clientBuilder = registry.getBean(FeignClientBuilder::class.java)

        registry.registerBeanDefinition(
            beanName,
            RootBeanDefinition(FeignClientFactoryBean::class.java, ConstructorArgumentValues().apply {
                addGenericArgumentValue(requester)
                addGenericArgumentValue(clazz)
                addGenericArgumentValue(clientBuilder)
            }, null).apply {
                isPrimary = true
            }
        )
    }

    private fun registerRSocketRequester(
        registry: BeanDefinitionRegistry,
        requesterName: String,
        client: Map.Entry<String, FeignClientMappingDto>
    ) {
        registry.registerBeanDefinition(
            requesterName,
            RootBeanDefinition(RSocketRequesterFactoryBean::class.java, ConstructorArgumentValues().apply {
                addGenericArgumentValue((registry as DefaultListableBeanFactory).getBean(RSocketRequester.Builder::class.java))
                addGenericArgumentValue(client.value.host)
                addGenericArgumentValue(client.value.port)
            }, null)
        )
    }

    private fun registerFeignClientBuilder(registry: BeanDefinitionRegistry) {
        registry.registerBeanDefinition(
            "feignClientBuilder",
            RootBeanDefinition(FeignClientBuilder::class.java).apply {
                scope = ConfigurableBeanFactory.SCOPE_PROTOTYPE
            }
        )
    }

    /**
     * RSocketRequesterBuilder
     * RSocketMessageHandler
     */
    private fun dependsOnRSocketStrategies(registry: BeanDefinitionRegistry) {
        val bean = (registry as DefaultListableBeanFactory).getBean(RSocketStrategies::class.java)
        registry.registerBeanDefinition(
            "rSocketRequesterBuilder",
            RootBeanDefinition(RSocketRequesterBuilderFactoryBean::class.java, ConstructorArgumentValues().apply {
                addGenericArgumentValue(bean)
            }, null).apply {
                scope = ConfigurableBeanFactory.SCOPE_PROTOTYPE
            })
    }

    private fun registerRSocketStrategies(registry: BeanDefinitionRegistry) =
        registry.registerBeanDefinition("rSocketStrategies", RootBeanDefinition(RSocketStrategiesFactoryBean::class.java))


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