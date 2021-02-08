package org.study.account.config

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContextHolder : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        CONTEXT = applicationContext
    }

    companion object {
        private var CONTEXT: ApplicationContext? = null
        fun <T> getBean(beanClass: Class<T>): T {
            return CONTEXT!!.getBean(beanClass)
        }
    }
}