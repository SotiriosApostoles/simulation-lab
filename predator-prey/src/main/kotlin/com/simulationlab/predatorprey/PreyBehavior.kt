package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.Action
import com.simulationlab.core.Behavior
import com.simulationlab.core.Entity
import com.simulationlab.core.Move
import com.simulationlab.core.Position
import com.simulationlab.core.Remove
import com.simulationlab.core.SimulationState
import com.simulationlab.core.Update
import com.simulationlab.core.wander

class PreyBehavior : Behavior {
    override fun decide(entity: Entity, state: SimulationState): List<Action> {

        val currentEnergy = entity.energy.getOrElse { 0 }

        if (currentEnergy == 0)
            return listOf(Remove(entity.id))

        val newPosition = wander(entity, state)

        return listOf(
            Update(entity.id, entity.properties + ("energy" to currentEnergy-1)),
            Move(entity.id, newPosition)
        )
    }
}