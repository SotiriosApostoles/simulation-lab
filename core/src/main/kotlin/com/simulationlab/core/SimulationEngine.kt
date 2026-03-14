package com.simulationlab.core

class SimulationEngine(val state: SimulationState, val behaviors: Map<EntityId, Behavior>) {

    fun tick(): SimulationState {
        val actions = state.entities.flatMap { entity ->
            val behavior: Behavior? = behaviors[entity.id]
            behavior?.decide(entity, state) ?: emptyList<Action>()
        }

        val removes = actions.filterIsInstance<Remove>()
        val moves = actions.filterIsInstance<Move>()
        val spawns = actions.filterIsInstance<Spawn>()

        val removeIds = removes.map { it.entityId }.toSet()
        val afterRemoves = state.entities.filter { it.id !in removeIds }

        val movesByEntity = moves.associateBy { it.entityId }
        val afterMoves = afterRemoves.map { entity ->
            movesByEntity[entity.id]?.let { entity.copy(position = it.newPosition) } ?: entity
        }
        val afterSpawns = afterMoves + spawns.map { it.entity }

        return SimulationState(afterSpawns, state.tick+1, state.width, state.height)
    }
}