# Architecture Map

## Current Modules

### `core`
Core simulation abstractions and engine. Zero external dependencies beyond the standard library and Arrow.

**Contains:**
- Domain types: `Position`, `EntityId`, `Entity`, `Action` (sealed: `Move`, `Spawn`, `Remove`)
- State: `SimulationState` (immutable world snapshot)
- Logic: `Behavior` (fun interface, Strategy pattern), `SimulationEngine` (tick-based state transitions)

**Depends on:** nothing

## Planned Modules

| Module | Responsibility | Depends on |
|---|---|---|
| `domain` | Shared domain concepts (entity types, environment rules) | `core` |
| `application` | Orchestration, use cases, simulation lifecycle | `core`, `domain`, infrastructure interfaces |
| `infrastructure` | Persistence, external I/O, rendering | `core`, `domain` |
| `cli` | Command-line interface, user interaction | `application` |

## Dependency Rules

```mermaid
graph TD
    cli --> application
    application --> domain
    application --> infrastructure
    infrastructure --> domain
    domain --> core
    cli --> core
```

- `core` has **no outbound dependencies** — it is the innermost layer
- `domain` depends only on `core`
- `application` orchestrates domain and infrastructure but does not implement I/O directly
- `infrastructure` implements interfaces defined in `application` or `domain`
- `cli` is the outermost layer — depends on `application` for use cases
- **No circular dependencies** between modules

## Runtime Flow

```mermaid
sequenceDiagram
    participant CLI
    participant Engine as SimulationEngine
    participant Behavior as Behavior (Strategy)
    participant State as SimulationState

    CLI->>Engine: tick()
    Engine->>State: read entities
    loop for each entity
        Engine->>Behavior: decide(entity, state)
        Behavior-->>Engine: List<Action>
    end
    Engine->>State: apply actions (remove, move, spawn)
    Engine-->>CLI: new SimulationState (tick + 1)
```
