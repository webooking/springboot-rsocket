package org.study.account.config

import io.r2dbc.pool.ConnectionPool
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator


@Configuration
class DatabaseConfig {
    @Bean
    fun initializer(connectionPool: ConnectionPool): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionPool)
        initializer.setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("db/migration/V1_init.sql")))
        return initializer
    }
}