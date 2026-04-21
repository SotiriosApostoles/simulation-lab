package com.simulationlab.core

interface BehaviorError {
    val entityId: EntityId
}

data class EntityHasNoType(override val entityId: EntityId) : BehaviorError
data class EntityHasNoEnergy(override val entityId: EntityId) : BehaviorError
