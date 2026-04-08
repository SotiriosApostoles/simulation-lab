package com.simulationlab.predatorprey

import arrow.core.getOrElse
import com.simulationlab.core.Entity
import com.simulationlab.core.Move
import com.simulationlab.core.Position
import com.simulationlab.core.Remove
import com.simulationlab.core.SimulationState
import com.simulationlab.core.Spawn
import com.simulationlab.core.Update
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.math.abs

class PredatorBehaviorTest : FunSpec({
    test("should remove predator when energy is zero") {
        val predator = createPredator(Position(5, 5), 0)

        val initialState = SimulationState(
            listOf(predator),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)

        actions.size shouldBe 1
        actions[0].shouldBeTypeOf<Remove>()
    }

    test("should lose energy when no prey nearby") {
        val predator = createPredator(Position(5, 5), 5)
        val prey = createPrey(Position(5, 6), 100)

        val initialState = SimulationState(
            listOf(predator, prey),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        (actions[0] as Update).entityId shouldBe predator.id
        (actions[0] as Update).properties["energy"] shouldBe 4
        actions[1].shouldBeTypeOf<Move>()
    }

    test("should hunt prey at same position") {
        val predator = createPredator(Position(5, 5), 5)
        val prey = createPrey(Position(5, 5), 100)

        val initialState = SimulationState(
            listOf(predator, prey),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)

        actions.size shouldBe 3
        actions[0].shouldBeTypeOf<Remove>()
        (actions[0] as Remove).entityId shouldBe prey.id
        actions[1].shouldBeTypeOf<Update>()
        (actions[1] as Update).entityId shouldBe predator.id
        (actions[1] as Update).properties["energy"] shouldBe 10
        actions[2].shouldBeTypeOf<Move>()
    }

    test("should not hunt predator at same position") {
        val predator1 = createPredator(Position(5, 5), 5)
        val predator2 = createPredator(Position(5, 5), 5)

        val initialState = SimulationState(
            listOf(predator1, predator2),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator1, initialState)

        actions.size shouldBe 2
        actions shouldNotContain Remove(entityId = predator2.id)
    }

    test("should move to adjacent cell") {
        val predator = createPredator(Position(5, 5), 5)

        val initialState = SimulationState(
            listOf(predator),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)
        val dx = abs((actions[1] as Move).newPosition.x - predator.position.x)
        val dy = abs((actions[1] as Move).newPosition.y - predator.position.y)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        actions[1].shouldBeTypeOf<Move>()
        (dx + dy) shouldBe 1
    }

    test("should reproduce if energy exceeds threshold") {
        val predator = createPredator(Position(5, 5), 20)

        val initialState = SimulationState(
            listOf(predator),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)

        actions.size shouldBe 3
        actions[0].shouldBeTypeOf<Spawn>()
        (actions[0] as Spawn).entity.energy.getOrElse { 0 } shouldBe 10
        actions[1].shouldBeTypeOf<Update>()
        (actions[1] as Update).properties["energy"] shouldBe 9
        actions[2].shouldBeTypeOf<Move>()
    }

    test("should set state to Feeding when prey is at same position") {
        val predator = createPredator(Position(5, 5), 5)
        val prey = createPrey(Position(5, 5), 100)

        val initialState = SimulationState(
            listOf(predator, prey),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)

        actions.size shouldBe 3
        actions[0].shouldBeTypeOf<Remove>()
        (actions[0] as Remove).entityId shouldBe prey.id
        actions[1].shouldBeTypeOf<Update>()
        (actions[1] as Update).properties["state"] shouldBe PredatorState.Feeding
        (actions[1] as Update).properties["energy"] shouldBe 10
        actions[2].shouldBeTypeOf<Move>()
    }

    test("should set state to Hunting when no prey nearby") {
        val predator = createPredator(Position(5, 5), 5)

        val initialState = SimulationState(
            listOf(predator),
            0,
            10,
            10
        )
        val predatorBehavior = PredatorBehavior(12)
        val actions = predatorBehavior.decide(predator, initialState)

        actions.size shouldBe 2
        actions[0].shouldBeTypeOf<Update>()
        (actions[0] as Update).properties["state"] shouldBe PredatorState.Hunting
        (actions[0] as Update).properties["energy"] shouldBe 4
        actions[1].shouldBeTypeOf<Move>()
    }
})