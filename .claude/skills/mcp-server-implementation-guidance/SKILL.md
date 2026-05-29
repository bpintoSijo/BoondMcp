---
name: mcp-server-implementation-guidance
description: Use when implementing or modifying the Sijo MCP Server module in apps/mcp-boondmanager.
---

# MCP Server Implementation Guidance

Guidance for implementing or modifying the Sijo MCP Server — the gateway between AI agents and the BoondManager APIs.

## Scope

Applies only to `apps/mcp-boondmanager`.

Do not modify `apps/web-ui`, `apps/agent-api`, shared packages, or infrastructure unless the user explicitly asks. Stay inside the module.

## Read First

Before any implementation, read — in this order:

1. `README.md` — module overview, architecture, tool catalog.
2. `CLAUDE.md` — authoritative implementation rules and architecture boundaries.
3. Any `docs/` files present in the module, when they exist:
   - implementation plan
   - architecture
   - API contract
   - tool specifications
   - testing strategy

## Implementation Approach

Follow these steps in order:

1. Start with configuration and `@ConfigurationProperties` (typed, validated records).
2. Define DTOs as records before wiring any behavior.
3. Build the WebClient layer as a thin HTTP adapter — no business logic.
4. Implement each `@Tool` as a small, independently testable unit.
5. Mock BoondManager HTTP responses before depending on a live server.
6. Add tests as each tool layer is introduced — not after the fact.
7. Validate all query parameters via `UriComponentsBuilder` before sending requests.

## Fixed Decisions

Non-negotiable technical choices:

| Concern | Decision |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.x |
| HTTP client | Spring WebFlux `WebClient` |
| AI tooling | Spring AI — `@Tool` annotations + `ToolCallbackProvider` |
| Injection style | Constructor injection only |
| Data objects | Records preferred, immutable |
| Transport | Streamable HTTP (MCP) |
| Build tool | Maven |

## Guardrails

Hard rules — never break these:

- Do not call BoondManager directly from the Python backend; all access goes through the MCP Server.
- Do not expose raw BoondManager endpoints through MCP tools.
- Do not hardcode URLs, credentials, ports, or reference-data enums.
- Do not implement AI reasoning, scoring, or LangGraph logic inside the MCP Server.
- Do not use field injection (`@Autowired` on fields).
- Do not append null parameters to HTTP requests.
- Do not create source code unless the user requests implementation.
