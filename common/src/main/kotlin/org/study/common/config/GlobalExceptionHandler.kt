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