---
typora-root-url: ./assets
---





1. 打包
     auth-client.jar (interface, model)
     auth-core.jar (springboot jar)

account-common-client.jar
account-order-client.jar
account-balance-client.jar
account-core.jar

​	 2.2 调用
底层实现是RSocketRequester
feign + kotlin coroutines + RSocketRequester



https://github.com/spring-projects/spring-framework/issues/22462



# 1 custom feign client

模仿框架`feign`实现`rsocket client`。比如，order调用account

- 服务提供者（account）打包时，单独打一个`account-order-client.jar`。只包含接口定义`interface`,没有实现类！！！
- 服务消费者（order）
  - 依赖`account-order-client.jar`
  - 由框架`feign`将interface翻译成`RSocketRequester`调用的代码。使用动态代理，自动生成模板代码

# 2 AOP

## 2.1 what

![img](/032704592829_0pe617dbmwowpyyo1ycgw.png)



## 2.2 when

![Spring Boot AOP - javatpoint](/pring-boot-aop.png)

## 2.3 how

AOP 底层使用动态代理

|               | interface | object |
| ------------- | --------- | ------ |
| JDK动态代理   | √         |        |
| CGLIB动态代理 |           | √      |

# 3 JDK 动态代理

## 3.1 demo


```
# 1 待测接口
interface UserService {
    fun sayHello(name: String): String
    fun others(): Unit
}

# 2 测试
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class UserServiceSpec : StringSpec({
    "jdk dynamic proxy"{
        val greeting = "Hello Tom!"

        val handler = InvocationHandler { _, method, args ->
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        }
        val instance = Proxy.newProxyInstance(
            this::class.java.classLoader,
            arrayOf(UserService::class.java),
            handler
        ) as UserService


        instance.others().shouldBe(Unit)

        val message = instance.sayHello("yuri")

        message.shouldBe(greeting)
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
```



## 3.2 How to continue a suspend function in a dynamic proxy in the same coroutine?

```
suspend fun a(){
	proxy(suspend b())
}
```

```
package org.study.feign.proxy

import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation

typealias SuspendInvoker = suspend (method: Method, arguments: List<Any?>) -> Any?

private interface SuspendFunction {
    suspend fun invoke(): Any?
}

private val SuspendRemover = SuspendFunction::class.java.methods[0]

@Suppress("UNCHECKED_CAST")
fun <C : Any> proxy(contract: Class<C>, invoker: SuspendInvoker): C =
    Proxy.newProxyInstance(contract.classLoader, arrayOf(contract)) { _, method, arguments ->
        val continuation = arguments.last() as Continuation<*>
        val argumentsWithoutContinuation = arguments.take(arguments.size - 1)
        SuspendRemover.invoke(object : SuspendFunction {
            override suspend fun invoke() = invoker(method, argumentsWithoutContinuation)
        }, continuation)
    } as C
```



```
interface UserService {
    suspend fun sayHello(name: String): String
    suspend fun others(): Unit
}

package org.study.feign

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.study.feign.proxy.proxy

class UserServiceSpec : StringSpec({
    "jdk dynamic proxy"{
        val greeting = "Hello Tom!"

        val instance = proxy(UserService::class.java){method, args ->
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        }


        instance.others().shouldBe(Unit)

        val message = instance.sayHello("yuri")

        message.shouldBe(greeting)
    }
    "launch"{
        val greeting = "Hello Tom!"

        val instance = proxy(UserService::class.java){method, args ->
            delay(1000)
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        }

        launch {
            instance.others().shouldBe(Unit)
        }
        launch {
            val message = instance.sayHello("yuri")

            message.shouldBe(greeting)
        }
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
```



## 3.3 Manually inject objects into the Spring container

1. 创建bean
2. 注入到spring 容器 https://www.jianshu.com/p/07ddd99cb58b
3. 扫描源码，判断需要实例化哪些接口？
   1). 为接口添加特定注解 `@RSocketClient`
   2). 判断方法是否有注解 `@MessageMapping`
4. 使用自定义注解 + `@Import`，将整个业务逻辑嵌入spring 容器中

### 3.3.1 dynamic proxy bean

```
val greeting = "Hello Tom!"

proxy(UserService::class.java) { method, args ->
    delay(1000)
    log.info("Invoked method: {}, args: {}", method.name, args)

    if ("sayHello" == method.name) {
        log.info("method: {}, return String: {}", method.name, greeting)
        greeting
    } else {

    }
}
```

### 3.3.2 inject into spring container

```
BeanFactoryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory) 

@Suppress("ControlFlowWithEmptyBody")
beanFactory.registerSingleton(
  metadata.className, 
  proxy(UserService::class.java
) { method, args ->
    delay(1000)
    log.info("Invoked method: {}, args: {}", method.name, args)

    if ("sayHello" == method.name) {
        log.info("method: {}, return String: {}", method.name, greeting)
        greeting
    } else {

    }
})
```



### 3.3.3 client interfaces

```
import org.springframework.messaging.handler.annotation.MessageMapping
import org.study.feign.annotation.Anonymous
import org.study.feign.annotation.RSocketClient

@RSocketClient
interface UserService {

    @Anonymous
    @MessageMapping("say.hello")
    suspend fun sayHello(name: String): String

    @MessageMapping("others")
    suspend fun others(): Unit
}

```



### 3.3.4 scanner client interfaces

```
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
```



### 3.3.5 validate methods

```
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
```



### 3.3.6 custom annotation & @Import

```
import org.springframework.context.annotation.Import
import org.study.feign.core.RSocketClientsRegistrar
import kotlin.reflect.KClass

@Retention
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Import(RSocketClientsRegistrar::class)
annotation class EnableRSocketClients(
    vararg val value: String = [],
    val basePackages: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = []
)

```



### 3.3.7 testing

```
@SpringBootApplication
@EnableRSocketClients
class App
```

## 3.4 rsocket client









# 4 dependency

https://docs.gradle.org/current/userguide/java_library_plugin.html



![java library ignore deprecated test](/java-library-ignore-deprecated-test.png)