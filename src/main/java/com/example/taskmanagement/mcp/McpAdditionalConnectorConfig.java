package com.example.taskmanagement.mcp;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpAdditionalConnectorConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> mcpConnectorCustomizer() {
        return factory -> {
            Connector mcpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            mcpConnector.setPort(8081);
            factory.addAdditionalTomcatConnectors(mcpConnector);
        };
    }
}
