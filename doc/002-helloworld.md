---
typora-root-url: ./assets
---

# 1 构建

## 1.1 项目入口

`build.gradle.kts`

```
plugins {
    idea
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

group = "org.study"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kotestVersion = "4.4.0.RC2"
    val springmockkVersion = "3.0.1"

    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-rsocket")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring-jvm:$kotestVersion")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.7"
    }
}
```



## 1.2 .gitignore

隐藏不必要的文件（夹）
intellij idea编译过程中会自动生成一些文件（夹），需要分享给其他开发人员的，才需要交给git管理。其他的，都可以隐藏掉。咱们自己开发的时候，也不需要看到，全都交给开发工具（intellij idea）维护

```
.gradle/
gradle/wrapper/gradle-wrapper.jar
build/
out/
.kotlintest/
logs/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea/
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

### VS Code ###
.vscode/
```



## 1.3 执行gradle的task

### 1.3.1 wrapper

执行`wrapper`命令，会自动下载指定的gradle版本

![wrapper-task](/wrapper-task.png)

### 1.3.2 刷新

点击屏幕右侧Gradle的`刷新`按钮，会根据`build.gralde.kts`的配置，自动构建项目

# 2 编码

## 2.1 Server

### 2.1.1 resources/application.yml

```
spring:
  rsocket:
    server:
      port: 7000
```

### 2.1.2 model

```
package org.study.account.model

import java.time.LocalDateTime
import java.util.*

data class User(
    val id:String = UUID.randomUUID().toString(),
    val name:String,
    val age: Int,
    val createTime: LocalDateTime = LocalDateTime.now()
)
```

### 2.1.3 controller

```
package org.study.account.controller

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import reactor.core.publisher.Mono

@Controller
class UserController {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val users = listOf<User>(
        User(name = "peter", age = 18),
        User(name = "yuri", age = 28),
        User(name = "henry", age = 38),
        )

    @MessageMapping("request-response")
    fun requestResponse(name: String): Mono<User> {
        log.info("Received request-response request: {}", name)
        val user = users.first { it.name == name }
        log.info("request-response Response: {}", user)
        return Mono.just<User>(user)
    }
}
```

### 2.1.4 main

运行main函数，启动服务

```
package org.study.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
```

## 2.2 测试

```
1. 打开terminal，进入account项目根目录

2. 下载rsc.jar
wget -O rsc.jar https://github.com/making/rsc/releases/download/0.4.2/rsc-0.4.2.jar            

3. 访问接口`request-response`
java -jar rsc.jar --debug --request --data "yuri" --route request-response tcp://localhost:7000

4. 查看日志与控制台输出的内容
2021-01-27 14:29:57.489 DEBUG --- [actor-tcp-nio-1] i.r.FrameLogger : sending -> 
Frame => Stream ID: 1 Type: REQUEST_RESPONSE Flags: 0b100000000 Length: 30
Metadata:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 10 72 65 71 75 65 73 74 2d 72 65 73 70 6f 6e 73 |.request-respons|
|00000010| 65                                              |e               |
+--------+-------------------------------------------------+----------------+
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 79 75 72 69                                     |yuri            |
+--------+-------------------------------------------------+----------------+
2021-01-27 14:29:57.579 DEBUG --- [actor-tcp-nio-1] i.r.FrameLogger : receiving -> 
Frame => Stream ID: 1 Type: NEXT_COMPLETE Flags: 0b1100000 Length: 116
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 7b 22 69 64 22 3a 22 35 35 63 30 66 30 30 62 2d |{"id":"55c0f00b-|
|00000010| 30 66 36 38 2d 34 61 38 37 2d 61 33 30 33 2d 33 |0f68-4a87-a303-3|
|00000020| 63 34 66 39 32 62 31 34 63 30 39 22 2c 22 6e 61 |c4f92b14c09","na|
|00000030| 6d 65 22 3a 22 79 75 72 69 22 2c 22 61 67 65 22 |me":"yuri","age"|
|00000040| 3a 32 38 2c 22 63 72 65 61 74 65 54 69 6d 65 22 |:28,"createTime"|
|00000050| 3a 22 32 30 32 31 2d 30 31 2d 32 37 54 31 34 3a |:"2021-01-27T14:|
|00000060| 32 39 3a 34 35 2e 33 34 39 33 39 37 22 7d       |29:45.349397"}  |
+--------+-------------------------------------------------+----------------+
{"id":"55c0f00b-0f68-4a87-a303-3c4f92b14c09","name":"yuri","age":28,"createTime":"2021-01-27T14:29:45.349397"}
```

# 3 扩展

## 3.1 profile

### 3.1.1 custom enum

```
package org.study.account.model

enum class CustomProfile {
    dev,sit,prod
}
```

### 3.1.2 application-{profile}.yml

![image-20210127203829236](/image-20210127203829236.png)



### 3.1.3 active

```
spring:
  profiles:
    active: dev
```

## 3.2 延迟初始化

```
spring:
  main:
    lazy-initialization: true
```

## 3.3 日志



## 3.4 端口



# 4 测试

## 4.1 dependencies

```
    val kotestVersion = "4.4.0.RC2"
    val springmockkVersion = "3.0.1"

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring-jvm:$kotestVersion")
```

## 4.2 test/resouces/application.yml

```
spring:
  main:
    lazy-initialization: true
  rsocket:
    server:
      port: 7000
```

## 4.3 @Bean RSocketRequester

```
package org.study.account.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester

@Configuration
class RSocketConfig {

    @Bean
    fun requester(
        builder: RSocketRequester.Builder,
        @Value("\${spring.rsocket.server.port}") port: Int
    ): RSocketRequester = builder.tcp("localhost", port)
}
```

## 4.4 UserControllerSpec

```
package org.study.account

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.User
import reactor.kotlin.test.test

@SpringBootTest
class UserControllerSpec(val requester: RSocketRequester): StringSpec({
    "request-response"{
        requester
            .route("request-response")
            .data("yuri")
            .retrieveMono(User::class.java)
            .test()
            .expectNextMatches { it.age == 28 }
            .expectComplete()
            .verify()
    }
})
```

## 4.5 table-driven testing

```
package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.User
import reactor.kotlin.test.test

@SpringBootTest
class UserControllerSpec(val requester: RSocketRequester): StringSpec({
    "Table-Driven testing request-response"{
        forAll(
            row("peter", 18),
            row("yuri", 28),
            row("henry", 38),
        ){name, age ->
            requester
                .route("request-response")
                .data(name)
                .retrieveMono(User::class.java)
                .test()
                .expectNextMatches { it.age == age }
                .expectComplete()
                .verify()
        }
    }
})
```



# 5 RSocket协议

## 5.1 OSI模型和TCP/IP协议

OSI(Open System Interconnection，开放式通信互联)

TCP/IP协议栈，简化OSI七层模型

![TCP/IP](/tcp_ip.png)

## 5.2 什么是RSocket协议

RSocket位于OSI模型的第5/6层（会话层，表示层）

RSocket的传输层，可以选择以下协议：

- TCP
- WebSocket
- Aeron

## 5.3 RSocket设计目标

- 传输数据
- 流量控制（背压）
  - client按需取数据
  - **讨价还价**。如果client请求的数据太多/太快，server端可以控制client的请求速度，并告知client
  - client可以提前终止读取数据，并告知server
- 自动恢复连接。比如手机进地铁之后，网络断开一段时间，其他协议需要重新建立连接

## 5.4 模型

| Server           | Client                                     | Desc                                                         |
| ---------------- | ------------------------------------------ | ------------------------------------------------------------ |
| Request Response | `Mono<T> retrieveMono(Class<T> dataType);` | Perform a requestResponse exchange.<br/>If the return type is Mono\<Void\>, the Mono <br/>will complete after all data is consumed. |
| Fire and Forget  | `Mono<Void> send();`                       | Perform a fireAndForget sending the<br/> provided data and metadata.Returns:<br/>a completion that indicates if the payload<br/> was sent successfully or not. Note, <br/>however that is a one-way send and<br/> there is no indication of whether or <br/>how the even was handled on the<br/> remote end. |
| Stream/Channel   | `Flux<T> retrieveFlux(Class<T> dataType);` | Perform an requestStream or a <br/>requestChannel exchange depending <br/>on whether the request input is single <br/>or multi-payload.<br/>If the return type is Flux\<Void\>, the Flux <br/>will complete after all data is consumed. |

# 6 coroutines

`coroutine`在kotlin中，特指`官方提供的一套线程API`。优点：

- 基于kotlin的语言优势，代码简洁
- 可以用`看起来`同步的代码，实现异步与回调的效果。即：`非阻塞式挂起`

## 6.1 为什么coroutine不会阻塞线程？

占用资源较多的函数，请先使用`suspend`关键字标记

```
suspend fun loadImageRemotely(){
...
}
```

### 6.1.1 `suspend`有什么用

- 标记耗时较长的函数
- 若正确调用了协程，那么，`suspend`标记的函数不会阻塞主线程
  - 如果是我写的实现，使用单元测试，验证是否会阻塞主线程
  - 别人写的，同样需要验证

最终，形成一套`机制`，让所有耗时任务，都自动转到后台执行，执行完毕后，再**<u>自动切换回来</u>**。

suspend只是一个标记，什么都没做。真正实现`非阻塞式挂起`的，是函数里的代码。

反模式，eg.

### ![image-20210128103647010](/image-20210128103647010.png)6.1.2 怎么实现自动切换？

设当前线程为A，其他线程为S。假设A的任务耗时较长，则，

- 将A转入后台运行。同时，切换到线程S

- A执行完毕后，通知主线程

### 6.1.3 思考

挂起后，线程A是否依然在阻塞执行？

## 6.2 练习题

使用协程实现一个网络请求（简单模拟）

- 等待5秒，然后，提示加载成功

- 等待时显示 Loading
- 请求成功让 Loading 消失
- 让你的代码看上去像单线程

## 6.3 coroutine是轻量级线程吗？

> 不是

用`Java`的`Executors`也可以使用相同的功能，只是代码稍微多点

```
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LotsOfCoroutinesSpec : StringSpec({
    "test the kotlin official example"{// 5s 548ms
        repeat(100_000) {
            launch {
                delay(5000L)
                print(".")
            }
        }
    }
    "test the example of Java version"{// 5s 291ms
        val executor = Executors.newSingleThreadScheduledExecutor()
        val task = Runnable {
            print(".")
        }
        repeat(100_000) {
            executor.schedule(task, 5L, TimeUnit.SECONDS)
        }
        executor.shutdown()
        executor.awaitTermination(10L, TimeUnit.SECONDS)
    }
})
```

## 6.4 重构 request-response

### 6.4.1 dependencies

```
dependencies {
    val kotestVersion = "4.4.0.RC2"
    val springmockkVersion = "3.0.1"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.boot:spring-boot-starter-rsocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring-jvm:$kotestVersion")
}
```



### 6.4.2 Controller

```
package org.study.account.controller

import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService

@Controller
class UserController(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("request-response")
    suspend fun requestResponse(name: String): User {
        log.info("Received request-response request: {}", name)
        val user = userService.findUserByName(name).awaitFirst()

        log.info("request-response Response: {}", user)
        return user
    }
}
```

### 6.4.3 Service

```
package org.study.account.service

import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import org.study.account.model.User
import reactor.core.publisher.Mono

@Service
class UserService(val users: List<User>) {

    suspend fun findUserByName(name: String): Mono<User> = mono {
        users.first { it.name == name }
    }
}
```

### 6.4.4 testing

> 继续使用之前写好的测试代码















