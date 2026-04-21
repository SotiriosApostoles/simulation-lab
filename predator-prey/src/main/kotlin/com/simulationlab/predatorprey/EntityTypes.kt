package com.simulationlab.predatorprey

import arrow.core.Option
import arrow.core.toOption
import com.simulationlab.core.Entity
import com.simulationlab.core.EntityId
import com.simulationlab.core.Position
import java.util.UUID

enum class EntityType {
    PREY, PREDATOR
}

fun createPrey(position: Position, energy: Int): Entity {
    return Entity(
        EntityId(UUID.randomUUID()),
        position,
        mapOf("type" to EntityType.PREY, "energy" to energy)
    )
}

fun createPredator(position: Position, energy: Int): Entity {
    return Entity(
        EntityId(UUID.randomUUID()),
        position,
        mapOf("type" to EntityType.PREDATOR, "energy" to energy)
    )
}

val Entity.type: Option<EntityType> get() = (properties["type"] as? EntityType).toOption()

val Entity.energy : Option<Int> get() = (properties["energy"] as? Int).toOption()