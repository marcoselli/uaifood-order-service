package br.edu.uaifood.orders.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@EntityScan("br.edu.uaifood.orders.domain.model")
@EnableJpaRepositories("br.edu.uaifood.orders.domain.repository")
class DatabaseConfig 