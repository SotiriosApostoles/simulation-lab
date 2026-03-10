# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Modular simulation platform in Kotlin — a learning project in architecture, design patterns, and functional programming. Goal: extensible platform for ecosystems, traffic, and colony simulations.

**This is a teaching-first project.** The user learns by writing code with guidance. Don't generate full implementations — break into small steps, ask user to implement, then review. See `docs/Prompt_Learn_Kotlin.md` for full mentoring guidelines.

## Build Commands

```bash
mvn clean compile              # Build all modules
mvn test                       # Run all tests
mvn clean verify               # Full build with tests
mvn -pl core test              # Test single module
mvn -pl core test -Dtest="ClassName"  # Run single test class
```

## Tech Stack

- Kotlin 2.1.10, JVM target 21 (OpenJDK 21)
- Maven multi-module project
- Kotest 5.9.1 (testing) + MockK 1.13.16 (mocking)
- Arrow 2.1.2 (Option/Either for error handling)

## Architecture

Multi-module Maven project. Currently only `core/` exists; future modules planned (simulation-engine, domain, application, infrastructure, cli).

**Enforce Clean/Hexagonal Architecture boundaries:**
- Domain has no outbound dependencies
- Application depends on Domain and Infrastructure interfaces
- No circular dependencies between modules

**Design patterns to apply:** Strategy (simulation behaviors), Observer (events), Factory (components), Builder (config DSLs), State (lifecycle), Template Method (simulation steps).

## Coding Conventions

- **Immutability first** — data classes, `val`, immutable collections
- **Pure functions** in domain logic — no side effects
- **Arrow for error handling** — `Option` over nulls, `Either` over exceptions
- **Test-first** with Kotest — behavior-focused test names (e.g., "should reduce prey population when predators hunt")
- **Small commits** — one logical change per commit

## Code Review Format

When reviewing user code, use: Strengths / Concerns / Suggestions / Refactoring Opportunities / Architecture Impact.

## Architecture Artifacts

Maintain as the project evolves:
- `ARCHITECTURE_MAP.md` — module responsibilities and dependencies
- `docs/adr/` — Architecture Decision Records for major decisions
- Diagrams (Mermaid/ASCII) for module and runtime interactions

## Project Phases

1. Architecture & setup (current)
2. Core simulation engine
3. First simulation (predator-prey)
4. Event system & extensibility
5. Advanced behaviors & AI strategies
