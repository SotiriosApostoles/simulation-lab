package com.simulationlab.predatorprey

import com.simulationlab.core.BehaviorError
import com.simulationlab.core.EntityId

sealed interface PredatorPreyError : BehaviorError

data class NoPreyToHunt(override val entityId: EntityId) : PredatorPreyError