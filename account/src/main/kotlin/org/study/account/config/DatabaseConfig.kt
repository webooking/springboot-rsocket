package org.study.account.config

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@Configuration
@EnableR2dbcRepositories
class DatabaseConfig(val properties: R2dbcProperties) {
    @Bean(destroyMethod = "dispose")
    @Primary
    fun connectionPool(): ConnectionPool {
        val connectionFactoryOptions = connectionFactoryOptions()
        val connectionFactory = ConnectionFactories.get(connectionFactoryOptions)

        val configuration = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(properties.pool.maxIdleTime)
            .initialSize(properties.pool.initialSize)
            .maxSize(properties.pool.maxSize)
            .build()
        return ConnectionPool(configuration)
    }

    private fun connectionFactoryOptions(): ConnectionFactoryOptions {
        val optionsFromUrl = ConnectionFactoryOptions.parse(properties.url)
        return ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DATABASE, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.DATABASE))
            .option(ConnectionFactoryOptions.DRIVER, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.DRIVER))
            .option(ConnectionFactoryOptions.HOST, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.HOST))
            .option(ConnectionFactoryOptions.PORT, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.PORT))
            .option(ConnectionFactoryOptions.PROTOCOL, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.PROTOCOL))
            .option(ConnectionFactoryOptions.USER, properties.username)
            .option(ConnectionFactoryOptions.PASSWORD, properties.password)
            .build()
    }
}