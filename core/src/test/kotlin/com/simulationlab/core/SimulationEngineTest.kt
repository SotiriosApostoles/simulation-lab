package com.simulationlab.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.util.*
import kotlin.math.E

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
})