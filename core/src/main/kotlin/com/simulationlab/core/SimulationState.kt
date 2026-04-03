package com.simulationlab.core

data class SimulationState(
    val entities: List<Entity>,
    val tick: Int,
    val width: Int,
    val height: Int,
    val events: List<Event> = emptyList()
)

fun interface Behavior {
    fun decide(entity: Entity, state: SimulationState): List<Action>
}

fun wander(entity: Entity, state: SimulationState): Position {
    val randomCoord = (0..3).random()

    val newPosition =
        when (randomCoord) {
            0 -> (entity.position + Position(1, 0))
            1 -> entity.position + Position(-1, 0)
            2 -> entity.position + Position(0, 1)
            3 -> entity.position + Position(0, -1)
            else -> entity.position + Position(0, 0)
        }

    val clampedPosition = Position(
        newPosition.x.coerceIn(0, state.width - 1),
        newPosition.y.coerceIn(0, state.height - 1)
    )

    return clampedPosition
}

fun SimulationState.entitiesAt(position: Position): List<Entity> {
    return entities.filter { it.position == position }
}
