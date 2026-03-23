package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.*

class PredatorBehavior(val reproductionThreshold: Int) : Behavior {
    override fun decide(entity: Entity, state: SimulationState): List<Action> {

        val currentEnergy = entity.energy.getOrElse { 0 }
        val baseEnergy = if (canReproduce(entity)) currentEnergy / 2 else currentEnergy

        if (currentEnergy == 0)
            return listOf(Remove(entity.id))

        val preys = state.entitiesAt(entity.position)
            .filter { entity -> entity.type.getOrElse { EntityType.PREDATOR } == EntityType.PREY }

        val actions =
            buildList {
                if (canReproduce(entity)) add(reproduce(entity, state))
                if ( preys.isEmpty() ) {
                    add(Update(entity.id, entity.properties + ("energy" to baseEnergy - 1)))
                }
                else {
                    add(Remove(preys[0].id))
                    add(Update(entity.id, entity.properties + ("energy" to baseEnergy + 5)))
                }
                add(Move(entity.id, wander(entity, state)))
            }

        return actions
    }

    fun canReproduce(entity: Entity): Boolean {
        return entity.energy.getOrElse { 0 } > reproductionThreshold
    }

    fun reproduce(entity: Entity, state: SimulationState): Spawn {
        val halfEnergy = entity.energy.getOrElse { 0 } / 2
        val offSpring = createPredator(wander(entity, state), halfEnergy)
        return Spawn(offSpring)
    }
}