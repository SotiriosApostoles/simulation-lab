package com.simulationlab.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.util.*

class SimulationEngineTest : FunSpec({

    test("should increment tick on each step") {
        val initialState = SimulationState(
            emptyList(),
            0,
            10,
            10
        )

        val engine = SimulationEngine(initialState, emptyMap())

        engine.tick().tick shouldBe 1
    }

    test("should move entity to new position") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0, 0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val behavior = Behavior { entity, state ->
            listOf(Move(EntityId(myUuid), Position(1, 1)))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().entities.first { entity.id == EntityId(myUuid) }.position shouldBe Position(1, 1)
    }

    test("should remove entity") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0, 0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val behavior = Behavior { entity, state ->
            listOf(Remove(EntityId(myUuid)))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().entities shouldBe emptyList()
    }

    test("should spawn new entity") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0, 0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val otherUuid = UUID.randomUUID()

        val spawnedEntity = Entity(
            EntityId(otherUuid),
            Position(0, 1),
            emptyMap()
        )

        val behavior = Behavior { entity, state ->
            listOf(Spawn(spawnedEntity))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().entities shouldContain spawnedEntity
    }

    test("should leave entity unchanged when no behavior is mapped") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0, 0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val otherUuid = UUID.randomUUID()

        val behavior = Behavior { entity, state ->
            listOf(Remove(EntityId(otherUuid)))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(otherUuid) to behavior)
        )

        engine.tick().entities shouldContain entity
    }

    test("should emit EntityRemoved event when entity is removed") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0, 0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val behavior = Behavior { entity, state ->
            listOf(Remove(entity.id))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().events shouldContain EntityRemoved(entity)
    }

    test("should emit EntityUpdated event when entity is updated") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0, 0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val behavior = Behavior { entity, state ->
            listOf(Update(entity.id, entity.properties))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().events shouldContain EntityUpdated(entity.id)
    }

    test("should emit EntityMoved event when entity is moved") {
        val myUuid = UUID.randomUUID()

        val oldPosition = Position(0,0)

        val entity = Entity(
            EntityId(myUuid),
            oldPosition,
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val newPosition = Position(0,1)

        val behavior = Behavior { entity, state ->
            listOf(Move(entity.id, newPosition))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().events shouldContain EntityMoved(entity.id, oldPosition, newPosition)
    }

    test("should emit EntitySpawned event when entity is spawned") {
        val myUuid = UUID.randomUUID()

        val entity = Entity(
            EntityId(myUuid),
            Position(0,0),
            emptyMap<String, Any>()
        )

        val initialState = SimulationState(
            listOf(entity),
            0,
            10,
            10
        )

        val spawnedUuid = UUID.randomUUID()

        val spawnedEntity = Entity(
            EntityId(spawnedUuid),
            Position(0,1),
            emptyMap<String, Any>()
        )

        val behavior = Behavior { _, _ ->
            listOf(Spawn(spawnedEntity))
        }

        val engine = SimulationEngine(
            initialState,
            mapOf(EntityId(myUuid) to behavior)
        )

        engine.tick().events shouldContain EntitySpawned(spawnedEntity.id)
    }
})