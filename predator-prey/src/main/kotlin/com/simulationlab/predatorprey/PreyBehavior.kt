package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.*
import kotlin.math.abs

class PreyBehavior(val reproductionThreshold: Int) : Behavior {
    override fun decide(entity: Entity, state: SimulationState): List<Action> {

        val currentEnergy = entity.energy.getOrElse { 0 }
        val baseEnergy = if (canReproduce(entity)) currentEnergy / 2 else currentEnergy

        if (currentEnergy == 0)
            return listOf(Remove(entity.id))

        val actions =
            buildList {
                add(Update(entity.id, entity.properties + ("energy" to baseEnergy - 1)))
                if (canReproduce(entity)) add(reproduce(entity, state))
                add(Move(entity.id, if (isDangerNearby(entity, state)) flee(entity, state) else wander(entity, state)))
            }

        return actions
    }

    fun canReproduce(entity: Entity): Boolean {
        return entity.energy.getOrElse { 0 } > reproductionThreshold
    }

    fun reproduce(entity: Entity, state: SimulationState): Spawn {
        val halfEnergy = entity.energy.getOrElse { 0 } / 2
        val offSpring = createPrey(wander(entity, state), halfEnergy)
        return Spawn(offSpring)
    }

    private fun isDangerNearby(entity: Entity, state: SimulationState): Boolean {
        val removedEvents = state.events.filterIsInstance<EntityRemoved>()

        return removedEvents.any { event ->
            event.entity.type.isSome { it == EntityType.PREY } &&
                    abs(entity.position.x - event.entity.position.x) <= 1 &&
                    abs(entity.position.y - event.entity.position.y) <= 1
        }
    }

    private fun flee(entity: Entity, state: SimulationState): Position {
        val removedEntities = state.events.filterIsInstance<EntityRemoved>().map { it.entity }
        val threat = removedEntities.minBy {
            maxOf(
                abs(entity.position.x - it.position.x),
                abs(entity.position.y - it.position.y)
            )
        }

        val dx = entity.position.x - threat.position.x
        val dy = entity.position.y - threat.position.y

        if (dx == 0 && dy == 0) return wander(entity, state)

        val fleePositionX = (entity.position.x + dx.coerceIn(-1, 1)).coerceIn(0, state.width - 1)
        val fleePositionY = (entity.position.y + dy.coerceIn(-1, 1)).coerceIn(0, state.height - 1)

        return Position(fleePositionX, fleePositionY)
    }
}