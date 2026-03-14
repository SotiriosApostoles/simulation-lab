# ADR-001: Core Simulation Engine Design

**Status:** Accepted
**Date:** 2026-03-14

## Context

We need a core simulation engine that can support multiple simulation types (ecosystems, traffic, colonies). The engine must be generic enough to avoid coupling to any specific domain while providing a clear execution model for entity-based simulations.

Key requirements:
- Support arbitrary entity types with different behaviors
- Maintain a clear, reproducible simulation state at each step
- Align with functional programming principles (immutability, pure functions)
- Allow behaviors to be swapped or composed without modifying the engine

## Decision

### Grid-based world
Entities exist on a discrete 2D grid defined by `width` and `height`. Positions are integer coordinates. This simplifies spatial reasoning, neighbor lookups, and rendering compared to continuous coordinates.

### Simultaneous turns
All entities act in the same tick. The engine collects actions from every entity before applying any of them. This avoids order-dependent behavior where early-acting entities have an unfair advantage.

### Immutable state
`SimulationState` is an immutable snapshot. Each `tick()` produces a new state rather than mutating the existing one. This guarantees:
- No side effects in the simulation loop
- Easy state history (just keep previous states)
- Safe concurrency if needed later

### Strategy pattern for behaviors
`Behavior` is a `fun interface` that takes an `Entity` and the current `SimulationState`, returning a `List<Action>`. Each entity is mapped to a behavior externally via `Map<EntityId, Behavior>`. This decouples entity data from decision logic and allows runtime behavior swapping.

### Actions as a sealed interface
Entity intentions are expressed as `Action` subtypes (`Move`, `Remove`, `Spawn`). The engine resolves these into state changes. This separates "what an entity wants to do" from "how the world changes," enabling future conflict resolution (e.g., two entities moving to the same cell).

### Processing order
Actions are applied in a fixed order: removes first, then moves, then spawns. This prevents acting on entities that should be dead and ensures spawned entities don't act until the next tick.

## Consequences

### Positive
- Engine is simulation-agnostic — new simulation types only need new `Behavior` implementations
- Immutable state makes debugging and testing straightforward
- `fun interface` enables concise lambda syntax for behaviors
- Sealed `Action` gives compile-time exhaustiveness checks

### Trade-offs
- Grid-based world limits simulations that need continuous movement or fractional positioning
- Simultaneous turns require conflict resolution when multiple actions target the same entity or cell (not yet implemented)
- `Map<String, Any>` for entity properties is flexible but not type-safe — may need typed alternatives as complexity grows
- No event system yet — behaviors can't react to what other entities did in the same tick
