# ADR-003: Behavior State Machines and `Either`-based Error Handling

**Status:** Accepted
**Date:** 2026-05-02

## Context

After the event system landed (ADR — implicit, see `EntityRemoved` enrichment), behaviors started carrying real conditional logic: prey reacting to nearby `EntityRemoved` events, predators feeding vs. hunting, predators retaining a memory of where they last saw prey. Two pressures emerged:

1. **State proliferation inside `decide()`** — each behavior was branching on multiple conditions (energy, proximity, events) and producing different action sets. Without a named "mode," the branches were implicit and harder to test in isolation.
2. **Failure modes were silent** — `decide()` returned `List<Action>`. When an entity was malformed (missing `type`, missing `energy`) or a domain invariant was violated (a `Feeding` predator with no prey), the behavior had to choose between throwing, returning an empty list, or returning bogus actions. None were good.

Phase 5 of the project asked for AI-style behaviors (FSM, memory) and type-safe error handling. We needed to address both without leaking simulation-specific concepts into `core`.

## Decision

### Per-entity-type FSM as a sealed interface
Each entity type defines its own sealed FSM:

```kotlin
sealed interface PreyState {
    data object Wandering : PreyState
    data object Fleeing : PreyState
}

sealed interface PredatorState {
    data object Hunting : PredatorState
    data object Feeding : PredatorState
}
```

The states live in `predator-prey`, not `core`. We chose **separate sealed interfaces per entity type** rather than a single shared `EntityState` union, because:
- Compiler exhaustiveness on a `when (preyState)` would break the moment we added predator states to a shared hierarchy.
- Prey states and predator states have nothing meaningful in common — forcing them into the same type would be a coincidence, not a model.

### State stored in `properties`, accessed via Arrow `Option`
The FSM value is persisted in the same `Map<String, Any>` already used for `energy` and `type`, under the `"state"` key. Reads go through typed Arrow `Option` extensions:

```kotlin
val Entity.preyState: Option<PreyState> get() = (properties["state"] as? PreyState).toOption()
```

This keeps `core` ignorant of FSM concepts and confines the unsafe cast to one extension per accessor. The trade-off (unchecked `Any` storage) is the same one we accepted in ADR-002.

### Behavior memory via the same `properties` channel
Predator memory (`lastSeenPrey: Position`) follows the same pattern:

```kotlin
val Entity.lastSeenPrey: Option<Position> get() = (properties["lastSeenPrey"] as? Position).toOption()
```

Memory is written by emitting `Update` actions that include the new key; it survives across ticks because the engine applies the merged properties map to the entity. This avoids inventing a parallel "memory" concept in `core` — memory is just persisted state that the behavior happens to use as input on the next tick.

### `Either<BehaviorError, List<Action>>` at the `Behavior` interface
The interface signature is now:

```kotlin
fun interface Behavior {
    fun decide(entity: Entity, state: SimulationState): Either<BehaviorError, List<Action>>
}
```

Errors are explicit in the type system. Behaviors return `Left(error)` for malformed entities or violated invariants; the engine `fold`s — Lefts are logged and produce no actions, Rights contribute to the action stream:

```kotlin
behavior.decide(entity, state).fold(
    ifLeft = { error -> println("Behavior failed for entity ${entity.id}: $error"); emptyList() },
    ifRight = { it }
)
```

We picked **`Either` at the interface boundary** rather than handling errors internally because failure information needs to cross from the simulation into the engine — an internal `try/catch` or null-return loses the structured error and forces the engine to guess what went wrong.

### Extensible `BehaviorError` via marker interface in `core`
`BehaviorError` is a non-sealed marker interface in `core`:

```kotlin
interface BehaviorError {
    val entityId: EntityId
}
data class EntityHasNoType(override val entityId: EntityId) : BehaviorError
data class EntityHasNoEnergy(override val entityId: EntityId) : BehaviorError
```

Each simulation defines its own sealed hierarchy that extends it:

```kotlin
sealed interface PredatorPreyError : BehaviorError
data class NoPreyToHunt(override val entityId: EntityId) : PredatorPreyError
```

This was chosen over the alternatives:
- **Sealed `BehaviorError` in `core`** — would force `core` to know every possible simulation-specific error, breaking module boundaries.
- **Generic `Behavior<E : BehaviorError>`** — would propagate the type parameter through `SimulationEngine` and the `Map<EntityId, Behavior>` registration, polluting every call site for marginal benefit.

The marker interface gives us extensibility across module boundaries while still letting each simulation enjoy exhaustive matching within its own sealed hierarchy.

## Consequences

### Positive
- FSM states are first-class and named — `Wandering`/`Fleeing`/`Hunting`/`Feeding` show up in tests and review as exact `data object`s, not magic strings.
- Failure paths are part of the type. The engine cannot accidentally ignore a malformed entity; it has to handle the `Left`.
- New simulations get their own error hierarchy without modifying `core`. They keep exhaustive `when` matching inside their own module.
- Predator memory required no new concept in the engine — it's persisted state, written and read through the same channel as energy.

### Trade-offs
- Storing FSM states and memory as `Any` in `properties` means the unsafe cast in extensions is still our weakest type boundary. A typed component map remains the longer-term target if complexity grows.
- The current engine logs `Left`s with `println` and discards them. A real observability layer (structured logging, metrics) is out of scope but will be needed once simulations run unattended.
- `BehaviorError` being an open interface (not sealed) means `core` cannot exhaustively match on it. We lose exhaustiveness at the engine layer in exchange for cross-module extensibility — an explicit, conscious choice.
- Memory keys (`"state"`, `"lastSeenPrey"`) are stringly-typed inside the simulation. A typo at the write site is undetectable at compile time. Mitigation: keep all writes in the behavior file that owns the corresponding extension property.
