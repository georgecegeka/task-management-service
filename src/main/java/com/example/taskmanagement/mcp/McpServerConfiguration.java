package com.example.taskmanagement.mcp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * MCP Server Configuration using Spring Function Beans
 * This exposes Spring beans as MCP tools
 */
@Configuration
public class McpServerConfiguration {

    @Bean
    public McpToolRegistry mcpToolRegistry(ApplicationContext applicationContext) {
        Map<String, McpToolDefinition> tools = new HashMap<>();

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Description.class);
        beans.forEach((beanName, bean) -> {
            if (bean instanceof Function) {
                Description description = applicationContext.findAnnotationOnBean(beanName, Description.class);
                if (description != null) {
                    @SuppressWarnings("unchecked")
                    Function<Object, Object> function = (Function<Object, Object>) bean;
                    tools.put(beanName, new McpToolDefinition(
                            beanName,
                            description.value(),
                            function
                    ));
                }
            }
        });

        return new McpToolRegistry(tools);
    }

    public static class McpToolRegistry {
        private final Map<String, McpToolDefinition> tools;

        public McpToolRegistry(Map<String, McpToolDefinition> tools) {
            this.tools = tools;
        }

        public Set<String> getToolNames() {
            return tools.keySet();
        }

        public McpToolDefinition getTool(String name) {
            return tools.get(name);
        }

        public Map<String, McpToolDefinition> getAllTools() {
            return tools;
        }
    }

    public record McpToolDefinition(
            String name,
            String description,
            Function<Object, Object> function
    ) {}
}

