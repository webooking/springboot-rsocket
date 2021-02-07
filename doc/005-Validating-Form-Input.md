---
typora-root-url: ./assets
---

# 1 用途

- 验证client传入的参数
- 使用注解（声明式）配置校验逻辑
- 由框架根据配置的注解，自动实现繁琐的验证代码

# 2 功能

- 验证当前model
- 级联验证（一对一，一对多）
- 分组校验
- 自定义注解

# 3 Bean Validation

## 3.1 dependency

```
val validatorVersion = "6.2.0.Final"
implementation("org.hibernate.validator:hibernate-validator:$validatorVersion")
```

## 3.2 Model

```
data class CreateRequest(
   val username: String,
   @get:Min(18) val age: Int,
   val gender: Gender,
)
```

## 3.3 ControllerValidator

```
package org.study.account.service.validator

import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.study.account.model.User
import javax.validation.Valid

@Component
@Validated
class UserControllerValidator {
    fun create(@Valid request: User.CreateRequest) = request
}
```

## 3.4 The message thrown during the test

```
must be greater than or equal to 18
```

# 4 Cascade Validation

## 4.1 Story

- 一个用户只能使用一个手机号
- 手机号，分两部分：
  - countryCode
  - number

## 4.2 Model

```
import javax.validation.constraints.Pattern

data class Phone(
    @get:Pattern(regexp = "^\\+(1|86)$")
    val countryCode: String,
    @get:Pattern(regexp = "^\\d{10,11}$")
    val number: String
)
```

```
data class CreateRequest(
    val username: String,
    @get:Min(18) val age: Int,
    val gender: Gender,
    @get:Valid
    val phone: Phone,
)
```

## 4.3 resources/META-INF/validation.xml

```
<validation-config
        xmlns="http://xmlns.jcp.org/xml/ns/validation/configuration"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/validation/configuration
            http://xmlns.jcp.org/xml/ns/validation/configuration/validation-configuration-2.0.xsd"
        version="2.0">

    <property name="hibernate.validator.fail_fast">true</property>
</validation-config>
```



# ~~5 Group Validation~~

# 6 Custom Validation

## 6.1 Single Property

### 6.1.1 story

腿是偶数

```
data class User(
  ...
  
  @get:Odd
  val legs:Int,
)
```
### 6.1.2 Annotation

```
package org.study.account.validation.constraints

import org.study.account.validation.validator.OddValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Repeatable
@MustBeDocumented
@Constraint(validatedBy = [OddValidator::class])
annotation class Odd(
    val message: String = "腿的个数必须是偶数",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) {
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
    )
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class List(vararg val value: Odd)
}
```

### 6.1.3 ConstraintValidator

```
package org.study.account.validation.validator

import org.study.account.validation.constraints.Odd
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class OddValidator : ConstraintValidator<Odd, Int> {
    override fun isValid(value: Int, context: ConstraintValidatorContext): Boolean = value % 2 == 0
}
```

## 6.2 Multiple properties correlation verification

### 6.2.1 Story

| Age     | AgeBracket | 年龄段 |
| ------- | ---------- | ------ |
| [0,2)   | Baby       | 幼儿   |
| [2,13)  | Child      | 儿童   |
| [13,19] | Adolescent | 青少年 |

```
@AgeBracket
data class User(
  ...
  @get:Min(18) val age: Int,
  val ageBracket:String,
)
```

### 6.2.2 Annotation

```
package org.study.account.validation.constraints

import org.study.account.validation.validator.AgeBracketValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Repeatable
@MustBeDocumented
@Constraint(validatedBy = [AgeBracketValidator::class])
annotation class AgeBracket(
    val message: String = "年龄段的名称错误",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) {
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
    )
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class List(vararg val value: AgeBracket)
}
```



### 6.2.3 ConstraintValiator

```
package org.study.account.validation.validator

import org.study.account.model.User
import org.study.account.validation.constraints.AgeBracket
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class AgeBracketValidator : ConstraintValidator<AgeBracket, User> {
    override fun isValid(value: User, context: ConstraintValidatorContext): Boolean = when(value){
        is User.CreateRequest -> checkCreate(value)
        else -> false
    }

    private fun checkCreate(user: User.CreateRequest): Boolean = when(val age = user.age){
        in 0 until 2 -> isContains(user.ageBracket, "Baby", "幼儿")
        in 2 until 13 -> isContains(user.ageBracket, "Child", "儿童")
        else -> isContains(user.ageBracket, "Adolescent", "青少年")
    }
    private fun isContains(value:String, vararg names:String) = names.contains(value)
}
```







 







