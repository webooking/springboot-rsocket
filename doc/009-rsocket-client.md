---
typora-root-url: ./assets
---

# 1 架构图

![image-20210615133344278](/image-20210615133344278.png)

## 1.1 Feign

接口只是假象，Rsocket,http,websocket等协议，通过网络传输的都是报文。Feign可以把接口转译成报文：

- 是否需要传token
- encode 参数
- decode 返回值
- 自动添加kotlin的coroutine上下文环境
- 服务注册与发现，负载均衡等

## 1.2 Provider A

按照feign的标准，单独打包`A-client.jar`。

|                |                                                              |      |
| -------------- | ------------------------------------------------------------ | ---- |
| `A-client.jar` | 只包含接口，没有实现类<br />单独打包<br />不包含依赖的jar，不可以运行。通常体积是几KB |      |
| `A-model.jar`  | 对于`A-client.jar`，其中，接口的参数和返回值，必然是一些model，所以，索性将所有的model集中管理: dot/vo/entity/pojo |      |
| `A.jar`        | 不包含`A-client.jar`。因为spring-security的token对代码有侵入性，接口和实现类的参数不一样，所以，也不存在继承关系，自然，也不需要依赖`A-client.jar` <br />依赖`A-model.jar` <br />打包成`boot.jar`,可以运行。通常体积是十几MB |      |

## 1.3 Consumer

- 依赖`A-client.jar`，然后，把接口当作实现类直接调用。剩下的逻辑，交由feign维护
- 除了调用`provider A`的接口外，可以同时调用其他`provider`
- 也会打包成`boot.jar`，可以运行
- 也可以是`provider`，开放接口给其他服务调用

# 2 Feign framework

## 2.1 测试bean加载顺序

测试`@Import`与`EnableAutoConfiguration`的加载顺序

预计：`EnableAutoConfiguration > @Import` 

实际：:heavy_check_mark:

测试过程：

```
1. 请检查代码提交记录： 测试bean加载顺序
2. 运行 account，观察控制台输出
```

![image-20210616092040731](/image-20210616092040731.png)



## 2.2 读取`@FeignClientMapping`

测试反射与annotation

### 2.2.1 annotation

```
import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention //反射时可以读取
@MustBeDocumented
@Import(RSocketClientsRegistrar::class)
annotation class EnableRSocketClients(
    vararg val value: FeignClientMapping
)

@Retention
annotation class FeignClientMapping(
    val name: String,
    val host: String,
    val port: Int,
    vararg val classes: KClass<*>
)
```

### 2.2.2 model

```
data class FeignClientMapping(
    val host: String,
    val port: Int,
    @Suppress("ArrayInDataClass") val classes: Array<Class<*>>
)
```

### 2.2.3 解析

```
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.study.feign.model.FeignClientMapping as FeignClientMappingDto

class RSocketClientsRegistrar : ImportBeanDefinitionRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clients: Map<String, FeignClientMappingDto> = parseFeignClientMappings(importingClassMetadata)
        log.info("parseFeignClientMappings: $clients")
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
```

# 2.3 初始化bean，并注入context

### 2.3.1 RootBeanDefinition

RootBeanDefinition，注入一个单例

```
import org.slf4j.LoggerFactory

class RSocketClientBuilder {
    private val log = LoggerFactory.getLogger(this::class.java)

    constructor(){
        log.info("init class RSocketClientBuilder")
    }
}
```

```
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar

class RSocketClientsRegistrar : ImportBeanDefinitionRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun registerBeanDefinitions(
      importingClassMetadata: AnnotationMetadata, 
      registry: BeanDefinitionRegistry
    ) {
        if(!registry.containsBeanDefinition("rSocketClientBuilder")){
            log.info("has not a bean rSocketClientBuilder")
            registry.registerBeanDefinition(
              "rSocketClientBuilder",      
              RootBeanDefinition("org.study.feign.util.RSocketClientBuilder")
            )
        }
        log.info("Does have bean rSocketClientBuilder? ${
         registry.containsBeanDefinition("rSocketClientBuilder")
        }")
        
        super.registerBeanDefinitions(importingClassMetadata, registry)
    }
```

### 2.3.2 使用FactoryBean初始化RSocketStrategies

默认没有`BearerTokenAuthenticationEncoder`，如果注册成功，可以从上下文获得

```
import org.springframework.beans.factory.FactoryBean
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder

class RSocketStrategiesFactoryBean: FactoryBean<RSocketStrategies> {
    override fun getObject(): RSocketStrategies = RSocketStrategies.builder()
        .encoders {
            it.add(BearerTokenAuthenticationEncoder())
            it.add(Jackson2CborEncoder())
        }
        .decoders {
            it.add(Jackson2CborDecoder())
        }
        .build()

    override fun getObjectType(): Class<*> = RSocketStrategies::class.java
}
```

```
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder
import org.study.feign.util.RSocketClientBuilder
import org.study.feign.util.RSocketStrategiesFactoryBean

class RSocketClientsRegistrar : ImportBeanDefinitionRegistrar {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun registerBeanDefinitions(
      importingClassMetadata: AnnotationMetadata, 
      registry: BeanDefinitionRegistry) {
        if (!registry.containsBeanDefinition("rSocketStrategies")) {
            registry.registerBeanDefinition("rSocketStrategies", 
             RootBeanDefinition(RSocketStrategiesFactoryBean::class.java))
        }

        log.info("Does have bean rSocketStrategies? ${
         (registry as DefaultListableBeanFactory)
            .getBean(
              org.springframework.messaging.rsocket.RSocketStrategies::class.java)
            .encoders().map { it::class.java }
            .contains(BearerTokenAuthenticationEncoder::class.java)
         }")

        super.registerBeanDefinitions(importingClassMetadata, registry)
    }
}
```



### 2.3.3 创建`prototype`的`RSocketRequester.Builder`

![prototyp](/prototyp.png)

顺带还有一个问题：之前创建的moel都是没有参数的，现在，有属性咯！！！

```
registry.registerBeanDefinition(
            "rSocketRequesterBuilder",
            RootBeanDefinition(
              RSocketRequesterBuilderFactoryBean::class.java, 
              ConstructorArgumentValues().apply {
                addGenericArgumentValue(
                  (registry as DefaultListableBeanFactory)
                   .getBean(RSocketStrategies::class.java)
                )
            }, null).apply {
                scope = ConfigurableBeanFactory.SCOPE_PROTOTYPE
            })
```

```
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
```

### 2.3.4 bean冲突

`consumer`与`provider`交互，需要创建一个`RSocketRequester`;

每多加一个`provider`，就需要一个`RSocketRequester`;

测试本地`consumer`的接口，也需要创建一个`RSocketRequester`;

如何避免这些`RSocketRequester`起冲突？

答：给这些bean起不同的名字：

- provider。 "${providerName}RSocketRequester"
- consumer。"rSocketRequester"

### 2.3.5 接收方法的返回值

`kotlinx-coroutines-reactor`在`Mono<T>,Flux<T>`的基础上，做了扩展。对于kotlin更加友好，但是，参数类型改变后，意味着方法的返回值也比较特殊，`java`版的资料没有参考性了。那么，如何解析方法的返回值呢？

答： 查看`spring-messaging`的源码，发现底层已经实现了。不过，是`private`的方法，直接改装下，调用即可

1. 扩展`org.springframework.messaging.rsocket.RetrieveSpec`

```
<T> Mono<T> retrieveMono(ResolvableType dataType);
<T> Flux<T> retrieveFlux(ResolvableType dataType);
```

2. 重写`org.springframework.messaging.rsocket.DefaultRSocketRequester`

把对应的两个方法改为`public`的，且添加`@override`

3. 业务代码

```
suspend fun build(method: Method, arguments: List<Any?>): Any? = 
  withContext((arguments.last() as Continuation<*>).context) {
   ...
   
   val returnType = method.kotlinFunction!!.returnType
        if (returnType.toString().startsWith("kotlinx.coroutines.flow.Flow")) {
            specWithData
             .retrieveFlux<Any>(
               ResolvableType
                 .forMethodReturnType(method)
                 .generics[0]
              ).asFlow()
        } else {
            val type = ResolvableType.forMethodReturnType(method)
            val mono = specWithData.retrieveMono<Any>(type)
            if (type.rawClass == null || 
                type.rawClass!! == Unit.javaClass || 
                returnType.isMarkedNullable
            ) {
                mono.awaitFirstOrNull()
            } else {
                mono.awaitSingle()
            }
        }
}
    
```





















# 3 Feign client provider











# 4 feign client consumer













