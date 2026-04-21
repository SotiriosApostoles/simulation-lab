package com.simulationlab.predatorprey

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.simulationlab.core.*

class PredatorBehavior(val reproductionThreshold: Int) : Behavior {
    override fun decide(entity: Entity, state: SimulationState): Either<BehaviorError, List<Action>> {
        if (entity.type.getOrNull() == null) return EntityHasNoType(entity.id).left()
        if (entity.energy.getOrNull() == null) return EntityHasNoEnergy(entity.id).left()

        val preys = state.entitiesAt(entity.position)
            .filter { entity -> entity.type.getOrElse { EntityType.PREDATOR } == EntityType.PREY }
        val newState = if (preys.isNotEmpty()) PredatorState.Feeding else PredatorState.Hunting
        val currentEnergy = entity.energy.getOrElse { 0 }
        val baseEnergy = if (canReproduce(entity)) currentEnergy / 2 else currentEnergy

        if (currentEnergy == 0)
            return listOf(Remove(entity.id)).right()
        val actions =
            buildList {
                if (canReproduce(entity)) add(reproduce(entity, state))
                when (newState) {
                    PredatorState.Hunting -> {
                        add(
                            Update(
                                entity.id,
                                entity.properties + ("energy" to baseEnergy - 1) + ("state" to newState)
                            )
                        )
                        add(
                            Move(
                                entity.id,
                                entity.lastSeenPrey.getOrElse { null }
                                    ?.let { moveTowards(entity, it, state) }
                                    ?: wander(entity, state)
                            )
                        )
                    }

                    PredatorState.Feeding -> {
                        val prey = preys.firstOrNull() ?: return NoPreyToHunt(entity.id).left()
                        add(Remove(prey.id))
                        add(
                            Update(
                                entity.id,
                                entity.properties + ("energy" to baseEnergy + 5) + ("state" to newState) + ("lastSeenPrey" to entity.position)
                            )
                        )
                        add(Move(entity.id, wander(entity, state)))
                    }
                }
            }

        return actions.right()
    }

    fun canReproduce(entity: Entity): Boolean {
        return entity.energy.getOrElse { 0 } > reproductionThreshold
    }

    fun reproduce(entity: Entity, state: SimulationState): Spawn {
        val halfEnergy = entity.energy.getOrElse { 0 } / 2
        val offSpring = createPredator(wander(entity, state), halfEnergy)
        return Spawn(offSpring)
    }

    private fun moveTowards(entity: Entity, target: Position, state: SimulationState): Position {
        val dx = target.x - entity.position.x
        val dy = target.y - entity.position.y

        val targetPositionX = (entity.position.x + dx.coerceIn(-1, 1)).coerceIn(0, state.width - 1)
        val targetPositionY = (entity.position.y + dy.coerceIn(-1, 1)).coerceIn(0, state.height - 1)

        return Position(targetPositionX, targetPositionY)
    }
}