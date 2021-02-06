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
- model参数之间的逻辑校验
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







