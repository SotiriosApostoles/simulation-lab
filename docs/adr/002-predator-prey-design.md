# ADR-002: Predator-Prey Simulation Design

**Status:** Accepted
**Date:** 2026-03-30

## Context

The predator-prey simulation is the first concrete simulation built on top of the core engine. We needed to decide how entity-specific state (energy, type) would be stored and accessed, how behaviors would handle mutable-looking state in an immutable world, how reproduction would work, and where shared movement logic should live.

## Decision

### Entity properties via `Map<String, Any>` with typed accessors
Entity-specific state (energy, type) is stored in the generic `properties` map inherited from `core`. To avoid unsafe casts throughout the simulation code, Arrow `Option` extension properties (`Entity.type`, `Entity.energy`) wrap the map access:
```kotlin
val Entity.energy: Option<Int> get() = (properties["energy"] as? Int).toOption()
```
This keeps `core` generic while giving `predator-prey` a type-safe API at the boundary.

### `Update` action for property changes
Changing entity state (e.g., decreasing energy) is expressed as an `Update` action rather than `Remove` + `Spawn`. This was added to `core` as a general-purpose action. It replaces the full properties map, so callers must merge with the existing map: `entity.properties + ("energy" to newValue)`.

### Reproduction via configurable threshold
Both behaviors accept a `reproductionThreshold: Int` constructor parameter. When energy exceeds the threshold, the entity spawns an offspring with half its energy and keeps the other half (minus tick cost). This makes reproduction tunable per simulation run without modifying behavior code.

### Shared `wander()` function in `core`
Random adjacent movement with bounds clamping is extracted into `core` as a top-level function — not in `predator-prey` — because any simulation type could reuse it. This was preferable to duplicating the logic in both `PreyBehavior` and `PredatorBehavior`.

### Functional simulation runner with `runningFold`
`PredatorPreySimulation.run()` uses `runningFold` to accumulate the state history without mutation. The behavior map is rebuilt each tick from the current entities — necessary because entities are added and removed. `associate` maps each entity's `type` to the correct behavior instance.

### Action processing order extended
`Update` actions are processed after `Remove` and before `Move`, so energy changes apply to surviving entities only, and moves use the updated state.

## Consequences

### Positive
- Typed accessors isolate the `Map<String, Any>` unsafety to one place
- Configurable thresholds make simulations tunable without code changes
- `runningFold` produces full state history cleanly — useful for analysis and replay
- `wander()` in `core` is immediately reusable by future simulation types

### Trade-offs
- `Update` replaces the entire properties map — callers must remember to merge, or risk dropping properties
- Reproduction threshold is per-behavior-instance, not per-entity — all prey reproduce at the same threshold
- No conflict resolution yet — two predators can hunt the same prey in the same tick, both receiving energy but only one `Remove` being applied (the duplicate is silently ignored by `removeIf`)
