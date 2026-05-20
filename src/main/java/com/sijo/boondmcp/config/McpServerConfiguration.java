package com.sijo.boondmcp.config;

import com.sijo.boondmcp.tools.DictionaryTools;
import com.sijo.boondmcp.tools.PingPythonBackendTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfiguration {

    @Bean
    public ToolCallbackProvider boondToolCallbackProvider(
            PingPythonBackendTool pingPythonBackendTool,
            DictionaryTools dictionaryTools
    ) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(pingPythonBackendTool, dictionaryTools)
                .build();
    }
}