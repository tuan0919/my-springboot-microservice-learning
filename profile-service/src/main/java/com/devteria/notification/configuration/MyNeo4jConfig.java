package com.devteria.notification.configuration;

import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.context.annotation.Bean;

public class MyNeo4jConfig {
    @Bean
    Configuration cypherDslConfiguration() {
        return Configuration.newConfig().withDialect(Dialect.NEO4J_5).build();
    }
}
