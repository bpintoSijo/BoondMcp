# CLAUDE.md

## Project Overview

This project is an MCP (Model Context Protocol) Server built with:
- Java 21
- Spring Boot 4.x
- Spring AI

The MCP server acts as:
- MCP tool exposure layer
- transport abstraction layer
- protocol standardization layer
- audit/logging gateway
- bridge to Python AI backend

The MCP server is NOT responsible for:
- agent orchestration
- long reasoning workflows
- AI memory management
- business decision making
- complex AI state machines

Those responsibilities belong to the Python backend built with:
- FastAPI
- LangGraph

The Python backend is the AI orchestration layer.

---

## Business Goal

The goal of this project is to provide an AI assistant dedicated to HR and recruitment teams.

The AI assistant should help recruiters:
- simplify candidate search
- accelerate sourcing workflows
- reduce repetitive actions
- improve access to recruitment information
- streamline interactions with BoondManager
- assist decision-making during recruitment processes

The system provides a natural language interface for:
- candidate search
- mission exploration
- profile summarization
- recruitment automation

The MCP server acts as the standardized gateway between:
- AI agents
- recruitment tools
- business workflows
- BoondManager APIs

The project aims to build an enterprise-grade AI recruitment assistant platform that is:
- maintainable
- extensible
- observable
- secure-ready
- scalable

---

# Global Architecture

Frontend / MCP Client
↓
LLM / Agent
↓
MCP Server (Spring Boot + Spring AI)
↓
Python Backend (FastAPI + LangGraph)
↓
BoondManager API

---

# Architectural Principles

- Keep architecture pragmatic and minimal
- Prefer clarity over abstraction
- Avoid over-engineering
- Use explicit DTOs
- Keep deterministic behavior
- Maintain loose coupling between layers
- Keep MCP server thin (protocol layer only)

---

# MCP Server Responsibilities

The MCP server is responsible for:
- exposing MCP tools
- request validation
- transport handling
- structured logging
- correlation ID propagation
- tool execution orchestration (via Spring AI)
- communication with Python backend

It must NOT:
- implement business intelligence
- implement AI reasoning
- implement workflows
- contain domain decision logic

---

# MCP + Spring AI Design

This project uses:

- Spring AI MCP tool support
- @Tool annotations
- no custom MCP dispatcher
- no custom tool registry
- no MCP protocol reimplementation

Spring AI is responsible for:
- tool discovery
- tool execution
- tool invocation lifecycle

---

# MCP Tools Specification (BoondManager Domain)

All tools are business-level abstractions over BoondManager.

No raw API exposure is allowed.

---

## 1. Candidate Search & Exploration

### find_candidates
Search candidates based on filters:
- skills
- location
- availability
- experience
- contract type

---

### find_matching_candidates
Mission-based intelligent search.

---

### get_candidate_profile
Full enriched candidate profile:
- TAB_PROFIL
- TAB_DT
- skills
- availability

---

### get_candidate_summary
AI-generated short summary.

---

### check_candidate_availability
Availability validation based on:
- PARAM_DATEDISPO
- assignments

---

### get_candidate_assignments
Returns missions history.

---

## 2. Matching Intelligence (Python-driven)

### find_best_candidates
Returns ranked candidates for a mission.

Pipeline:
- Boond retrieval
- Python LangGraph ranking
- scoring + explanation

---

### rank_candidates
Re-ranks candidates based on mission context.

---

### explain_candidate
Explains candidate fit using Python backend reasoning.

---

## 3. Dictionary / Reference Data

### list_skills
### search_skills
### list_languages
### list_contract_types
### list_candidate_statuses

All rely on:
- `/application/dictionary`

---

## 4. Missions

### list_missions
### search_missions
### get_mission_details

---

## 5. Reporting (optional)

### summarize_recruitment_activity

---

## 6. Recommendation Engine

### recommend_candidates
AI-driven candidate recommendation.

### auto_shortlist_candidates
Automatic shortlist generation.

---

# Design Rules (CRITICAL)

- MCP tools MUST remain business-oriented
- No CRUD tools exposed
- No raw BoondManager API exposure
- All intelligence is delegated to Python LangGraph
- Spring AI only orchestrates tool execution

---

# Dictionary Dependency Rule

All candidate-related logic must rely on:
- skills
- languages
- contract types
- availability states
- evaluation statuses

No hardcoded enums allowed.

---

# Python Backend Responsibilities

The Python backend handles:
- AI orchestration
- LangGraph workflows
- ranking
- reasoning
- BoondManager interaction
- business intelligence

---

# Package Structure

com.sijo.boondmcp
├── audit
├── client
├── config
├── dto
├── exception
├── infrastructure
├── service
└── tools

---

# Java Conventions

- Java 21
- Spring Boot 4.x
- constructor injection only
- prefer records
- immutable objects
- explicit DTOs
- no field injection

---

# Tool Design Principles

Tools must:
- express business intent
- remain deterministic
- avoid CRUD naming
- be composable

Good:
- find_best_candidates
- explain_candidate

Bad:
- getCandidateById
- updateField

---

# Communication with Python Backend

- HTTP (WebClient)
- JSON DTOs
- centralized client
- strict timeouts
- explicit error handling
- typed payloads preferred

---

# Observability

Mandatory:
- correlationId (MDC)
- structured logs
- tool execution logs
- duration tracking

---

# Streaming Strategy

Must remain transport-agnostic.

Do NOT couple to SSE.

Future-ready for:
- streamable HTTP
- partial responses
- async tool execution

---

# Error Handling

- global exception handler
- explicit exception types
- no raw stack exposure
- consistent error format

---

# Configuration

All external systems must be configured via:

application.yml + @ConfigurationProperties

Never hardcode:
- URLs
- credentials
- ports

---

Example:

mcp:
python:
base-url: http://localhost:8000
timeout: 5s

---

# Security Preparation

Must remain compatible with:
- JWT
- RBAC
- API keys
- audit logs

---

# Audit Preparation

Prepare extension points for:
- tool execution history
- user tracking
- compliance logs

---

# Forbidden Patterns

Do NOT introduce:
- MCP custom dispatcher
- MCP registry
- ChatClient architecture
- Kafka
- CQRS
- event sourcing
- over-engineered DDD

---

# Development Philosophy

- simplicity first
- explicit over implicit
- minimal abstraction
- production-ready code
- maintainable architecture