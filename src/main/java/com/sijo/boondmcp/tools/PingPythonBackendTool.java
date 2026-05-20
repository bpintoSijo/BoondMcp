package com.sijo.boondmcp.tools;

import com.sijo.boondmcp.dto.PingRequest;
import com.sijo.boondmcp.dto.PingResponse;
import com.sijo.boondmcp.service.PingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PingPythonBackendTool {

    private final PingService pingService;

    public PingPythonBackendTool(PingService pingService) {
        this.pingService = pingService;
    }

    @Tool(
            name = "ping_python_backend",
            description = "Pings the Python (LangGraph) backend with a simple payload and returns the raw response."
    )
    public PingResponse ping(
            @ToolParam(description = "Free-text message forwarded to the Python backend.")
            String message
    ) {
        String payload = (message == null || message.isBlank()) ? "ping" : message;
        return pingService.ping(new PingRequest(payload));
    }
}