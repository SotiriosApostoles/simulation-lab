package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.Entity
import com.simulationlab.core.EntityId
import com.simulationlab.core.Move
import com.simulationlab.core.Position
import com.simulationlab.core.Remove
import com.simulationlab.core.SimulationEngine
import com.simulationlab.core.SimulationState
import com.simulationlab.core.Update
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
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

        val preyBehavior = PreyBehavior()
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

        val preyBehavior = PreyBehavior()
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

        val preyBehavior = PreyBehavior()

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

        val preyBehavior = PreyBehavior()

        val actions = preyBehavior.decide(prey, initialState)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        actions[1].shouldBeTypeOf<Move>()
        (actions[1] as Move).newPosition.x shouldBeGreaterThanOrEqual 0
        (actions[1] as Move).newPosition.y shouldBeGreaterThanOrEqual 0
    }
} )