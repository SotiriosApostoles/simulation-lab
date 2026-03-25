package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.*

class PredatorPreySimulation {
    companion object {
        const val PREY_INITIAL_ENERGY = 5
        const val PREDATOR_INITIAL_ENERGY = 8
    }

    fun createInitialState(width: Int, height: Int, preyCount: Int, predatorCount: Int): SimulationState {

        val prey = List(preyCount) { createPrey(randomPosition(width, height), PREY_INITIAL_ENERGY) }
        val predators = List(predatorCount) { createPredator(randomPosition(width, height), PREDATOR_INITIAL_ENERGY) }
        return SimulationState(prey + predators, 1, width, height)
    }

    fun randomPosition(width: Int, height: Int): Position {
        val randomX = (0 until width).random()
        val randomY = (0 until height).random()

        return Position(randomX, randomY)
    }

    fun run(
        state: SimulationState,
        preyBehavior: PreyBehavior,
        predatorBehavior: PredatorBehavior,
        ticks: Int
    ): List<SimulationState> {

        return (1..ticks).runningFold(state) { current, _ ->
            SimulationEngine(current, buildBehaviors(current, preyBehavior, predatorBehavior)).tick()
        }
    }

    fun buildBehaviors(
        state: SimulationState,
        preyBehavior: PreyBehavior,
        predatorBehavior: PredatorBehavior
    ): Map<EntityId, Behavior> =
        state.entities.associate { entity ->
            entity.id to when (entity.type.getOrElse { EntityType.PREDATOR }) {
                EntityType.PREY -> preyBehavior
                EntityType.PREDATOR -> predatorBehavior
            }
        }
}