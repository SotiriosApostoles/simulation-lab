package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.math.abs

class PreyBehaviorTest : FunSpec( {

    test("should remove prey when energy is zero") {
        val prey = createPrey(Position(5, 5), 0)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10
        )

        val preyBehavior = PreyBehavior(10)
        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 1
        actions[0].shouldBeTypeOf<Remove>()
    }

    test("should decrease energy by 1") {
        val prey = createPrey(Position(5, 5), 5)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10
        )

        val preyBehavior = PreyBehavior(10)
        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        actions[1].shouldBeTypeOf<Move>()
        (actions[0] as Update).properties["energy"] shouldBe 4
    }

    test("should move to adjacent cell") {
        val prey = createPrey(Position(5, 5), 5)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10
        )

        val preyBehavior = PreyBehavior(10)

        val actions = preyBehavior.decide(prey, initialState)

        val dx = abs((actions[1] as Move).newPosition.x - prey.position.x)
        val dy = abs((actions[1] as Move).newPosition.y - prey.position.y)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        actions[1].shouldBeTypeOf<Move>()
        ( dx + dy ) shouldBe 1
    }

    test("should clamp position to grid bounds") {
        val prey = createPrey(Position(0, 0), 5)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10
        )

        val preyBehavior = PreyBehavior(10)

        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        actions[1].shouldBeTypeOf<Move>()
        (actions[1] as Move).newPosition.x shouldBeGreaterThanOrEqual 0
        (actions[1] as Move).newPosition.y shouldBeGreaterThanOrEqual 0
    }

    test("should reproduce if energy exceeds threshold") {
        val prey = createPrey(Position(0, 0), 11)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10
        )

        val preyBehavior = PreyBehavior(10)

        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 3
        actions[0].shouldBeTypeOf<Update>()
        (actions[0] as Update).properties["energy"] shouldBe 4
        actions[1].shouldBeTypeOf<Spawn>()
        (actions[1] as Spawn).entity.energy.getOrElse { 0 } shouldBe 5
        actions[2].shouldBeTypeOf<Move>()
    }

    test("should wander when no danger") {
        val prey = createPrey(Position(5, 5), 10)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10,
            emptyList()
        )

        val preyBehavior = PreyBehavior(11)

        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 2
        actions[1].shouldBeTypeOf<Move>()

        val dx = abs((actions[1] as Move).newPosition.x - prey.position.x)
        val dy = abs((actions[1] as Move).newPosition.y - prey.position.y)

        dx shouldBeInRange 0..1
        dy shouldBeInRange 0..1
    }

    test("should flee when danger nearby") {
        val prey = createPrey(Position(5, 5), 10)
        val threat = createPrey(Position(4, 5), 10)

        val initialState = SimulationState(
            listOf(prey),
            0,
            10,
            10,
            listOf(EntityRemoved(threat))
        )

        val preyBehavior = PreyBehavior(11)

        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 2
        actions[1].shouldBeTypeOf<Move>()
        (actions[1] as Move).newPosition shouldBe Position(6, 5)
    }
} )