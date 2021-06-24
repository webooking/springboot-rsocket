package org.study.feign.factory

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.util.Assert
import org.study.feign.Anonymous
import org.study.feign.RSocketClient
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation

class FeignClientFactoryBean(
    private val rSocketRequester: RSocketRequester,
    private val clazz: Class<*>,
    private val builder: FeignClientBuilder,
) : FactoryBean<Any> {
    override fun getObject(): Any {
        val annotations = clazz.declaredAnnotations
        val rSocketClient = annotations.firstOrNull { it is RSocketClient }
        Assert.isTrue(
            rSocketClient != null,
            "${clazz.canonicalName} must have @${RSocketClient::class.java.canonicalName}"
        )
        Assert.isTrue(
            clazz.isInterface,
            "the @RSocketClient must be used only on an interface"
        )

        val suspectMethod = clazz.declaredMethods.firstOrNull { method ->
            method.declaredAnnotations.firstOrNull { it is MessageMapping } == null
        }
        if (suspectMethod != null) {
            throw IllegalArgumentException("${clazz.name}#${suspectMethod.name} must have annotation: @MessageMapping")
        }

        val isAnonymous = annotations.firstOrNull { it is Anonymous } != null

        builder.apply {
            anonymous = isAnonymous
            root = (rSocketClient!! as RSocketClient).value
            requester = rSocketRequester
        }

        return proxy(clazz) { method, arguments ->
            builder.build(method, arguments)
        }
    }

    override fun getObjectType(): Class<*> = clazz
}

typealias SuspendInvoker = suspend (method: Method, arguments: List<Any?>) -> Any?

private interface SuspendFunction {
    suspend fun invoke(): Any?
}

private val SuspendRemover = SuspendFunction::class.java.methods[0]

@Suppress("UNCHECKED_CAST")
fun <C : Any> proxy(contract: Class<C>, invoker: SuspendInvoker): C =
    Proxy.newProxyInstance(contract.classLoader, arrayOf(contract)) { _, method, arguments ->
        val continuation = arguments.last() as Continuation<*>
        SuspendRemover.invoke(object : SuspendFunction {
            override suspend fun invoke() = invoker(method, arguments.toList())
        }, continuation)
    } as C