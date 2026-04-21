package com.simulationlab.core

import arrow.core.Either

class SimulationEngine(val state: SimulationState, val behaviors: Map<EntityId, Behavior>) {

    fun tick(): SimulationState {
        val actions = state.entities.flatMap { entity ->
            val behavior = behaviors[entity.id] ?: return@flatMap emptyList<Action>()
            behavior.decide(entity, state).fold(
                ifLeft = { error ->
                    println("Behavior failed for entity ${entity.id}: $error")
                    emptyList()},
                ifRight = {it} )
        }

        val updates = actions.filterIsInstance<Update>()
        val removes = actions.filterIsInstance<Remove>()
        val moves = actions.filterIsInstance<Move>()
        val spawns = actions.filterIsInstance<Spawn>()

        val removeIds = removes.map { it.entityId }.toSet()
        val afterRemoves = state.entities.filter { it.id !in removeIds }
        val removedEntities = state.entities.filter { it.id in removeIds }

        val updatesByIds = updates.associateBy { it.entityId }
        val afterUpdates = afterRemoves.map { entity ->
            updatesByIds[entity.id]?.let { entity.copy(properties = it.properties) } ?: entity
        }

        val moveEvents = mutableListOf<EntityMoved>()
        val movesByIds = moves.associateBy { it.entityId }
        val afterMoves = afterUpdates.map { entity ->
            movesByIds[entity.id]?.let {
                moveEvents += EntityMoved(entity.id, entity.position, it.newPosition)
                entity.copy(position = it.newPosition) } ?: entity
        }

        val afterSpawns = afterMoves + spawns.map { it.entity }

        val events = removedEntities.map { EntityRemoved(it) } +
                updates.map { EntityUpdated(it.entityId) } +
                moveEvents +
                spawns.map { EntitySpawned(it.entity.id) }

        return SimulationState(afterSpawns, state.tick+1, state.width, state.height, events)
    }
}