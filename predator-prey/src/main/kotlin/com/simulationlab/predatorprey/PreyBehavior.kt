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

class PreyBehavior : Behavior {
    override fun decide(entity: Entity, state: SimulationState): List<Action> {

        val currentEnergy = entity.energy.getOrElse { 0 }

        if (currentEnergy == 0)
            return listOf(Remove(entity.id))

        val randomCoord = (0..3).random()

        val newPosition =
            when (randomCoord) {
                0 -> (entity.position + Position(1, 0))
                1 -> entity.position + Position(-1, 0)
                2 -> entity.position + Position(0, 1)
                3 -> entity.position + Position(0, -1)
                else -> entity.position + Position(0, 0)
            }

        val clampedPosition =  Position(
            newPosition.x.coerceIn(0, state.width - 1),
            newPosition.y.coerceIn(0, state.height - 1)
        )

        return listOf(
            Update(entity.id, entity.properties + ("energy" to currentEnergy-1)),
            Move(entity.id, clampedPosition)
        )
    }
}