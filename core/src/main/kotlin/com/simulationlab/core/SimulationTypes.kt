package com.simulationlab.core

import java.util.UUID

data class Position(val x: Int, val y: Int)

@JvmInline
value class EntityId(val uuid: UUID)

sealed interface Action

data class Move(val entityId: EntityId, val newPosition: Position) : Action
data class Spawn(val entity: Entity) : Action

data class Remove(val entityId: EntityId) : Action

data class Entity(val id: EntityId, val position: Position, val properties: Map<String, Any>)