package com.simulationlab.predatorprey

import arrow.core.getOrElse
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ranges.shouldBeIn
import io.kotest.matchers.shouldBe

class PredatorPreySimulationTest : FunSpec({
    test("should create initial state with correct entity counts") {
        val predatorPreySimulation = PredatorPreySimulation()
        val state = predatorPreySimulation.createInitialState(10, 10, 10, 2)

        state.entities.filter{ it.type.getOrElse { EntityType.PREDATOR } == EntityType.PREY }.size shouldBe 10
        state.entities.filter{ it.type.getOrElse { EntityType.PREY } == EntityType.PREDATOR }.size shouldBe 2
    }

    test("should create entities within grid bounds") {
        val predatorPreySimulation = PredatorPreySimulation()
        val state = predatorPreySimulation.createInitialState(10, 10, 10, 2)

        state.entities.map { it.position.x }.shouldForAll { it shouldBeIn 0 .. 10 }
        state.entities.map { it.position.y }.shouldForAll { it shouldBeIn 0 .. 10  }
    }

    test("should return state history with correct length") {
        val predatorPreySimulation = PredatorPreySimulation()
        val state = predatorPreySimulation.createInitialState(10, 10, 10, 2)

        val preyBehavior = PreyBehavior(8)
        val predatorBehavior = PredatorBehavior(12)

        val states = predatorPreySimulation.run(state, preyBehavior, predatorBehavior, 4)

        states.size shouldBe 5
    }

    test("should increment tick each step") {
        val predatorPreySimulation = PredatorPreySimulation()
        val state = predatorPreySimulation.createInitialState(10, 10, 10, 2)

        val preyBehavior = PreyBehavior(8)
        val predatorBehavior = PredatorBehavior(12)

        val states = predatorPreySimulation.run(state, preyBehavior, predatorBehavior, 4)

        states.map { it.tick}.shouldContainExactly((1..5).toList())
    }

    test("should reduce prey population when predators hunt") {
        val predatorPreySimulation = PredatorPreySimulation()
        val state = predatorPreySimulation.createInitialState(1, 1, 1, 1)

        val preyBehavior = PreyBehavior(8)
        val predatorBehavior = PredatorBehavior(12)

        val states = predatorPreySimulation.run(state, preyBehavior, predatorBehavior, 1)

        states.last().entities.filter{ it.type.getOrElse { EntityType.PREDATOR } == EntityType.PREY }.size shouldBe 0
        states.last().entities.filter{ it.type.getOrElse { EntityType.PREY } == EntityType.PREDATOR }.size shouldBe 1
    }
})