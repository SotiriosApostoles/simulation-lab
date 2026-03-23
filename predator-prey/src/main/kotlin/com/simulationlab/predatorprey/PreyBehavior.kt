package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.Action
import com.simulationlab.core.Behavior
import com.simulationlab.core.Entity
import com.simulationlab.core.EntityId
import com.simulationlab.core.Move
import com.simulationlab.core.Position
import com.simulationlab.core.Remove
import com.simulationlab.core.SimulationState
import com.simulationlab.core.Spawn
import com.simulationlab.core.Update
import com.simulationlab.core.wander
import java.util.UUID

class PreyBehavior(val reproductionThreshold : Int) : Behavior {
    override fun decide(entity: Entity, state: SimulationState): List<Action> {

        val currentEnergy = entity.energy.getOrElse { 0 }
        val baseEnergy = if (canReproduce(entity)) currentEnergy / 2 else currentEnergy

        if (currentEnergy == 0)
            return listOf(Remove(entity.id))

        val actions =
            buildList {
                add(Update(entity.id, entity.properties + ("energy" to baseEnergy - 1)))
                if (canReproduce(entity)) add(reproduce(entity, state))
                add(Move(entity.id, wander(entity, state)))
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
}