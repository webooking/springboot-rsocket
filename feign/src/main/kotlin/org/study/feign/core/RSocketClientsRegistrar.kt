package org.study.feign.core

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.EnvironmentAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.util.Assert
import org.springframework.util.ReflectionUtils
import org.study.feign.annotation.RSocketClient
import org.study.feign.client.UserService
import org.study.feign.proxy.proxy
import java.lang.reflect.Method


class RSocketClientsRegistrar : EnvironmentAware, ResourceLoaderAware, BeanFactoryPostProcessor {
    private val log = LoggerFactory.getLogger(this::class.java)

    private var environment: Environment? = null
    private var resourceLoader: ResourceLoader? = null

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory){
        object : ClassPathScanningCandidateComponentProvider(false, environment!!) {
            override fun isCandidateComponent(metadata: AnnotatedBeanDefinition): Boolean =
                metadata.metadata.isIndependent && !metadata.metadata.isAnnotation
        }.apply {
            addIncludeFilter(AnnotationTypeFilter(RSocketClient::class.java))
            resourceLoader = resourceLoader

            findCandidateComponents("org.study.feign.client")
                .filterIsInstance(AnnotatedBeanDefinition::class.java)
                .forEach {
                    val metadata = it.metadata
                    validateMethods(metadata)
                    registerRSocketClient(metadata, beanFactory)
                }
        }
    }

    private fun registerRSocketClient(metadata: AnnotationMetadata, beanFactory: ConfigurableListableBeanFactory) {
        val greeting = "Hello Tom!"

        @Suppress("ControlFlowWithEmptyBody")
        beanFactory.registerSingleton(metadata.className, proxy(UserService::class.java) { method, args ->
            delay(1000)
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        })
    }

    private fun validateMethods(annotationMetadata: AnnotationMetadata) {
        Assert.isTrue(
            annotationMetadata.isInterface,
            "the @${RSocketClient::class.java.name} annotation must be used only on an interface"
        )
        val clazz = Class.forName(annotationMetadata.className)
        ReflectionUtils.doWithMethods(clazz) { method: Method ->
            if (log.isDebugEnabled) {
                log.debug("validating ${clazz.name}#${method.name}")
            }
            val annotation = method.getAnnotation(MessageMapping::class.java)
            Assert.notNull(
                annotation,
                "you must use the @${MessageMapping::class.java.name} annotation on every method on ${clazz.name}."
            )
        }
    }
}