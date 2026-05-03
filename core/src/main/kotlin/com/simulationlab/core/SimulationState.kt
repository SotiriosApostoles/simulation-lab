package com.simulationlab.core

import arrow.core.Either

data class SimulationState<E>(
    val entities: List<Entity>,
    val tick: Int,
    val width: Int,
    val height: Int,
    val environment: E,
    val events: List<Event> = emptyList()
)

fun interface Behavior<E> {
    fun decide(entity: Entity, state: SimulationState<E>): Either<BehaviorError, List<Action>>
}

fun <E> wander(entity: Entity, state: SimulationState<E>): Position {
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

fun <E> SimulationState<E>.entitiesAt(position: Position): List<Entity> {
    return entities.filter { it.position == position }
}
