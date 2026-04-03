package com.simulationlab.core

import java.util.UUID

data class Position(val x: Int, val y: Int) {
    operator fun plus(pos: Position) = Position(x + pos.x, y + pos.y)
}

@JvmInline
value class EntityId(val uuid: UUID)

sealed interface Action

data class Move(val entityId: EntityId, val newPosition: Position) : Action
data class Spawn(val entity: Entity) : Action
data class Remove(val entityId: EntityId) : Action
data class Update(val entityId: EntityId, val properties: Map<String, Any>): Action

data class Entity(val id: EntityId, val position: Position, val properties: Map<String, Any>)

sealed interface Event

data class EntitySpawned(val entityId: EntityId) : Event
data class EntityRemoved(val entityId: EntityId) : Event
data class EntityUpdated(val entityId: EntityId) : Event
data class EntityMoved(val entityId: EntityId, val from: Position, val to: Position) : Event