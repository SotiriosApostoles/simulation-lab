package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.*

class PredatorBehavior : Behavior {
    override fun decide(entity: Entity, state: SimulationState): List<Action> {

        val currentEnergy = entity.energy.getOrElse { 0 }

        if (currentEnergy == 0)
            return listOf(Remove(entity.id))

        val preys = state.entitiesAt(entity.position)
            .filter { entity -> entity.type.getOrElse { EntityType.PREDATOR } == EntityType.PREY }

        val actions =
            if (preys.isEmpty()) {
                listOf(Update(entity.id, entity.properties + ("energy" to currentEnergy - 1)))
            } else {
                listOf(
                    Remove(preys[0].id),
                    Update(entity.id, entity.properties + ("energy" to currentEnergy + 5))
                )
            }

        val newPosition = wander(entity, state)

        return actions + Move(entity.id, newPosition)
    }
}