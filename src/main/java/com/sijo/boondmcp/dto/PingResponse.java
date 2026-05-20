package com.sijo.boondmcp.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record PingResponse(
        JsonNode payload
) {
}