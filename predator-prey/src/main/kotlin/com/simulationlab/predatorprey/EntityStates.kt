package com.simulationlab.predatorprey

import arrow.core.Option
import arrow.core.toOption
import com.simulationlab.core.Entity

sealed interface PreyState {
    data object Wandering : PreyState
    data object Fleeing : PreyState
}

sealed interface PredatorState {
    data object Hunting : PredatorState
    data object Feeding : PredatorState
}

val Entity.preyState : Option<PreyState> get() = (properties["state"] as? PreyState).toOption()
val Entity.predatorState : Option<PredatorState> get() = (properties["state"] as? PredatorState).toOption()


