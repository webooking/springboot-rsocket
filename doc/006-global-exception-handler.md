---
typora-root-url: ./assets
---

#  1 Exceptions

| Exceptions                                | desc                                    |
| ----------------------------------------- | --------------------------------------- |
| `ValidationException(filedName, message)` | 校验接口参数                            |
| `BusinessException(message)`              | 可以直接抛给用户的提示语                |
| `ErrorCodeException(code, message)`       | 需要前端处理的异常。 其中，`code`是标记 |
| `UnknownException(Throwable)`             | 代码BUG                                 |

> The `UnknownException` must be handled before publishing to the production environment

# 2 Unified Exception

```
data class UnifiedException(
    val reason: Reason? = null,
    val code: String? = null,
    val message: String? = null,
    val fieldName: String? = null,
    val cause: Exception? = null
)
enum class Reason {
    ValidationException, BusinessException, ErrorCodeException, UnknownException
}

class BusinessException(message: String) : java.lang.RuntimeException(message)
class ErrorCodeException(val code: String, message: String) : java.lang.RuntimeException(message)
class ValidationException(val fieldName: String, message: String) : java.lang.RuntimeException(message)
```

# 3 account

## 3.1 GlobalExceptionHandler

```
package org.study.account.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.rsocket.exceptions.CustomRSocketException
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import javax.validation.ConstraintViolationException

open class GlobalExceptionHandler(open val mapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Throws(CustomRSocketException::class)
    @MessageExceptionHandler(ConstraintViolationException::class)
    suspend fun handlerConstraintViolationException(ex: ConstraintViolationException): UnifiedException {
        val model = ex.constraintViolations.first()
        val temp = ValidationException(fieldName = model.propertyPath.toString(), message = model.message!!)
        log.error(temp.toString())
        throw temp.toRSocket(mapper)
    }
}

open class UnifiedException(
    val reason: Reason,
    open val code: String? = null,
    override val message: String? = null,
    open val fieldName: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {
    fun toRSocket(mapper: ObjectMapper) = CustomRSocketException(reason.errorCode, mapper.writeValueAsString(this))
}

enum class Reason(val errorCode: Int) {
    ValidationException(0x00000302),
    BusinessException(0x00000303),
    ErrorCodeException(0x00000304),
    UnknownException(0x00000305)
}

data class BusinessException(override val message: String) :
    UnifiedException(reason = Reason.BusinessException, message = message)

data class ErrorCodeException(override val code: String, override val message: String) :
    UnifiedException(reason = Reason.ErrorCodeException, code = code, message = message)

data class ValidationException(override val fieldName: String, override val message: String) :
    UnifiedException(reason = Reason.ValidationException, fieldName = fieldName, message = message)

data class UnknownException(override val cause: Throwable) :
    UnifiedException(reason = Reason.UnknownException, cause = cause)
```

## 3.2 Controller

```
@Controller
class UserController(
    val validator: UserControllerValidator,
    override val mapper: ObjectMapper
) : GlobalExceptionHandler(mapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(request: User.CreateRequest) {
        val validatedRequest = validator.create(request)
        log.info("create a user, request parameters: {}", validatedRequest)
    }
}
```

## 3.3 Model

```
selead class User{
    @AgeBracket
    data class CreateRequest(
        val username: String,
        @get:Min(18) val age: Int,
        val gender: Gender,
        @get:Valid
        val phone: Phone,
        @get:Odd
        val legs: Int,
        val ageBracket: String,
    )
}
```



## 3.4 Testing

```
package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.rsocket.exceptions.CustomRSocketException
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.config.ValidationException
import org.study.account.model.Gender
import org.study.account.model.Phone
import org.study.account.model.User
import reactor.kotlin.test.expectError
import reactor.kotlin.test.test
import java.util.*

@SpringBootTest
class CRUDSpec(val requester: RSocketRequester) : StringSpec({
    "create the user"{
        requester
            .route("create.the.user")
            .data(
                User.CreateRequest(
                    username = "yuri",
                    age = 18,
                    gender = Gender.Male,
                    phone = Phone(
                        countryCode = "+1",
                        number = "7785368920"
                    ),
                    legs = 1, //腿的个数必须是偶数
                    ageBracket = "Adolescent"
                )
            )
            .retrieveMono(Void::class.java)
            .test()
            .expectError(CustomRSocketException::class)
//            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
```

# 4 Q & A

1. 异常捕获的顺序？
2. 每个服务专属的异常如何处理？比如，cash（与微信，支付宝等交互的服务）中的`PaymentFailedException,PyamentTimedOutException`



# 5 common

## 5.1 build.gradle.kts

```
plugins {
    idea
    kotlin("jvm") version "1.4.21"
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

group = "org.study"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.4")
    implementation("io.rsocket:rsocket-core:1.1.0")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.springframework:spring-messaging:5.3.3")
    implementation("jakarta.validation:jakarta.validation-api:2.0.2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.8.1"
    }
}
```

## 5.2 GlobalExceptionHandler

```
package org.study.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.rsocket.exceptions.CustomRSocketException
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import javax.validation.ConstraintViolationException

open class GlobalExceptionHandler(open val mapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Throws(CustomRSocketException::class)
    @MessageExceptionHandler(ConstraintViolationException::class)
    fun handlerConstraintViolationException(ex: ConstraintViolationException) {
        val model = ex.constraintViolations.first()
        val temp = ValidationException(fieldName = model.propertyPath.toString(), message = model.message!!)
        log.error(temp.toString())
        throw temp.toRSocket(mapper)
    }

    @Throws(CustomRSocketException::class)
    @MessageExceptionHandler(BusinessException::class)
    fun handlerBusinessException(ex: BusinessException) {
        log.error(ex.toString())
        throw ex.toRSocket(mapper)
    }
    @Throws(CustomRSocketException::class)
    @MessageExceptionHandler(ErrorCodeException::class)
    fun handlerErrorCodeException(ex: ErrorCodeException) {
        log.error(ex.toString())
        throw ex.toRSocket(mapper)
    }

    @Throws(CustomRSocketException::class)
    @MessageExceptionHandler
    fun handlerUnknownException(ex: Throwable) {
        val temp = UnknownException(cause = ex)
        log.error(temp.toString())
        throw temp.toRSocket(mapper)
    }
}

open class UnifiedException(
    val reason: Reason,
    open val code: String? = null,
    override val message: String? = null,
    open val fieldName: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {
    fun toRSocket(mapper: ObjectMapper) = CustomRSocketException(reason.errorCode, mapper.writeValueAsString(this))
}

enum class Reason(val errorCode: Int) {
    ValidationException(0x00000301),
    BusinessException(0x00000302),
    ErrorCodeException(0x00000303),
    UnknownException(0x00000304)
}

data class BusinessException(override val message: String) :
    UnifiedException(reason = Reason.BusinessException, message = message)

data class ErrorCodeException(override val code: String, override val message: String) :
    UnifiedException(reason = Reason.ErrorCodeException, code = code, message = message)

data class ValidationException(override val fieldName: String, override val message: String) :
    UnifiedException(reason = Reason.ValidationException, fieldName = fieldName, message = message)

data class UnknownException(override val cause: Throwable) :
    UnifiedException(reason = Reason.UnknownException, cause = cause)
```



## 5.3 private maven repository

### 5.3.1 run nexus container

```
1. 下载nexus镜像
docker pull sonatype/nexus3:3.29.2

2. 启动容器
docker container run -d -p 5433:8081 --name nexus sonatype/nexus3:3.29.2

3. 进入容器
docker container exec -it nexus bash

4. 查看密码
bash-4.4$ cat nexus-data/admin.password 
07f716e1-9299-470e-bcfd-7764dd3732d4

5. 访问网址 http://localhost:5433/
用户名: admin
密码: 07f716e1-9299-470e-bcfd-7764dd3732d4

6. 修改登录密码（请记住密码）
```

### 5.3.2 create maven repository

![rsocket-repository](/rsocket-repository.png)



![](/Screen%20Shot%202021-02-07%20at%2011.23.48%20PM.png)

### 5.3.3 close container

关闭容器时，需要设置延迟关闭，让nexus存储数据。避免丢失数据

```
docker container stop --time=120 nexus
```

## 5.4 publish common to private maven repository

### 5.4.1 build.gradle.kts

```
plugins {
    idea
    `maven-publish`
    kotlin("jvm") version "1.4.21"
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

group = "org.study"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.4")
    implementation("io.rsocket:rsocket-core:1.1.0")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.springframework:spring-messaging:5.3.3")
    implementation("jakarta.validation:jakarta.validation-api:2.0.2")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.8.1"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            setUrl("http://localhost:5433/repository/rsocket")
            credentials {
                username = "admin"
                password = "123456"
            }
        }
    }
    publications {
        register("mavenKotlin", MavenPublication::class) {
            from(components["kotlin"])
            artifact(sourcesJar.get())
        }
    }
}
```

### 5.4.2 执行 publish task

![image-20210207234048164](/image-20210207234048164.png)

### 5.4.3 查看repository

![image-20210207234237388](/image-20210207234237388.png)



# 6 Refactoring account

## 6.1 add private maven repository

```
repositories {
    mavenCentral()
    maven(url = "http://localhost:5433/repository/rsocket/")
}
```

## 6.2 implement common

```
implementation("org.study:common:1.0.0")
```

## 6.3 收尾工作

- 删除本地的`GlobalExceptionHandler`
- import common的相关依赖
- 请重新执行测试代码，确保重构成功





