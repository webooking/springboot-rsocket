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





























# 3 Feign client provider











# 4 feign client consumer













