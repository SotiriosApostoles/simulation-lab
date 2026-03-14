package com.simulationlab.core

data class SimulationState(val entities: List<Entity>, val tick: Int, val width: Int, val height: Int)

fun interface Behavior {
    fun decide(entity: Entity, state: SimulationState): List<Action>
}