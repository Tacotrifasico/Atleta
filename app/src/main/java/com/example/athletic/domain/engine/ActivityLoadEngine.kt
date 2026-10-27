package com.example.athletic.domain.engine

import com.example.athletic.domain.model.ActivityIntensity
import com.example.athletic.domain.model.ActivityType
import com.example.athletic.domain.model.MuscleLoads
import com.example.athletic.domain.model.PilatesFocus

object ActivityLoadEngine {

    fun calculateLoads(
        type: ActivityType,
        durationMinutes: Int,
        intensity: ActivityIntensity = ActivityIntensity.Z2_AEROBIC,
        pilatesFocus: PilatesFocus? = null,
        stepCount: Int? = null,
        elevationGainM: Int? = null
    ): MuscleLoads {
        return when (type) {
            ActivityType.CYCLING -> calculateCyclingLoads(durationMinutes, intensity, elevationGainM)
            ActivityType.CYCLING_MTB_ENDURO -> calculateMtbLoads(durationMinutes, intensity, elevationGainM)
            ActivityType.RUNNING -> calculateRunningLoads(durationMinutes, intensity)
            ActivityType.SWIMMING -> calculateSwimmingLoads(durationMinutes, intensity)
            ActivityType.PILATES -> calculatePilatesLoads(durationMinutes, pilatesFocus, intensity)
            ActivityType.WALKING -> calculateWalkingLoads(stepCount ?: ((durationMinutes * 100).coerceAtLeast(1000)))
            ActivityType.HIKING -> calculateHikingLoads(durationMinutes, elevationGainM)
            ActivityType.KAYAK -> calculateKayakLoads(durationMinutes, intensity)
            ActivityType.SPORT -> MuscleLoads(legs = 3, core = 3, cardio = 4, impact = 3, globalFatigue = 3)
            ActivityType.CLASS -> MuscleLoads(legs = 2, core = 3, cardio = 3, impact = 2, globalFatigue = 2)
            ActivityType.OTHER -> MuscleLoads(cardio = 2, globalFatigue = 2)
        }
    }

    private fun calculateCyclingLoads(
        durationMinutes: Int,
        intensity: ActivityIntensity,
        elevationM: Int?
    ): MuscleLoads {
        // Cycling rules from user brief:
        // <45 min: low load
        // 45–90 min: moderate
        // 90–180 min: relevant
        // 3–4 h (180-240 min): high
        // >4 h (240-360 min): very high
        // >=6 h (>=360 min): exceptional endurance event! (legs 5, cardio 5, globalFatigue 5)
        val isExceptional = durationMinutes >= 360
        val isVeryHigh = durationMinutes >= 240
        val isHigh = durationMinutes >= 180
        val isRelevant = durationMinutes >= 90
        val isModerate = durationMinutes >= 45

        val legLoad = when {
            isExceptional -> 5
            isVeryHigh -> 5
            isHigh -> 4
            isRelevant -> 3
            isModerate -> 2
            else -> 1
        }

        val cardioLoad = when {
            isExceptional -> 5
            isVeryHigh -> 4
            intensity >= ActivityIntensity.Z4_THRESHOLD -> 5
            intensity == ActivityIntensity.Z3_TEMPO -> 4
            isHigh -> 3
            isRelevant -> 2
            else -> 1
        }

        val globalFatigue = when {
            isExceptional -> 5
            isVeryHigh -> 4
            isHigh -> 3
            isRelevant -> 2
            else -> 1
        }

        val coreLoad = if (isVeryHigh || (elevationM != null && elevationM > 800)) 2 else 1
        return MuscleLoads(
            legs = legLoad,
            cardio = cardioLoad,
            core = coreLoad,
            impact = 0, // Low impact compared to running
            grip = if (isVeryHigh) 2 else 1,
            globalFatigue = globalFatigue
        )
    }

    private fun calculateMtbLoads(
        durationMinutes: Int,
        intensity: ActivityIntensity,
        elevationM: Int?
    ): MuscleLoads {
        // MTB / Enduro includes grip, arms, shoulders, core, impact, and legs
        val base = calculateCyclingLoads(durationMinutes, intensity, elevationM)
        return base.copy(
            grip = (base.grip + 2).coerceAtMost(5),
            arms = 3,
            shoulders = 3,
            core = (base.core + 2).coerceAtMost(5),
            impact = 3,
            legs = (base.legs + 1).coerceAtMost(5),
            globalFatigue = (base.globalFatigue + 1).coerceAtMost(5)
        )
    }

    private fun calculateRunningLoads(
        durationMinutes: Int,
        intensity: ActivityIntensity
    ): MuscleLoads {
        val isLong = durationMinutes >= 90
        val isTempoOrIntervals = intensity >= ActivityIntensity.Z3_TEMPO
        val legLoad = when {
            isLong -> 5
            isTempoOrIntervals -> 4
            durationMinutes >= 45 -> 3
            else -> 2
        }
        val cardioLoad = when {
            intensity >= ActivityIntensity.Z4_THRESHOLD -> 5
            isTempoOrIntervals -> 4
            durationMinutes >= 45 -> 3
            else -> 2
        }
        val impactLoad = when {
            isLong -> 5
            isTempoOrIntervals -> 4
            else -> 3
        }
        val globalFatigue = when {
            isLong || (isTempoOrIntervals && durationMinutes >= 60) -> 4
            isTempoOrIntervals -> 3
            else -> 2
        }
        return MuscleLoads(
            legs = legLoad,
            cardio = cardioLoad,
            impact = impactLoad,
            core = 2,
            globalFatigue = globalFatigue
        )
    }

    private fun calculateSwimmingLoads(
        durationMinutes: Int,
        intensity: ActivityIntensity
    ): MuscleLoads {
        val isIntense = intensity >= ActivityIntensity.Z3_TEMPO || durationMinutes >= 60
        return if (isIntense) {
            MuscleLoads(
                shoulders = 4,
                back = 4,
                cardio = 4,
                core = 3,
                arms = 3,
                impact = 0,
                globalFatigue = 3
            )
        } else {
            // Gentle active recovery
            MuscleLoads(
                shoulders = 2,
                back = 2,
                cardio = 1,
                core = 1,
                impact = 0,
                globalFatigue = 1
            )
        }
    }

    private fun calculatePilatesLoads(
        durationMinutes: Int,
        focus: PilatesFocus?,
        intensity: ActivityIntensity
    ): MuscleLoads {
        val f = focus ?: PilatesFocus.CORE
        val coreLoad = when (f) {
            PilatesFocus.CORE -> 4
            PilatesFocus.FULL_BODY -> 3
            else -> 2
        }
        val shoulderLoad = when (f) {
            PilatesFocus.SHOULDERS -> 3
            PilatesFocus.FULL_BODY -> 2
            else -> 1
        }
        val legLoad = when (f) {
            PilatesFocus.LEGS -> 3
            PilatesFocus.FULL_BODY -> 2
            else -> 1
        }
        return MuscleLoads(
            core = coreLoad,
            shoulders = shoulderLoad,
            legs = legLoad,
            cardio = if (intensity >= ActivityIntensity.Z3_TEMPO) 2 else 1,
            impact = 0,
            globalFatigue = 2
        )
    }

    private fun calculateWalkingLoads(stepCount: Int): MuscleLoads {
        // <10k baseline habitual
        // 10–15k light
        // 15–20k moderate
        // >20k significant additional load
        return when {
            stepCount >= 20000 -> MuscleLoads(legs = 4, impact = 3, cardio = 2, globalFatigue = 3)
            stepCount >= 15000 -> MuscleLoads(legs = 3, impact = 2, cardio = 1, globalFatigue = 2)
            stepCount >= 10000 -> MuscleLoads(legs = 2, impact = 1, cardio = 1, globalFatigue = 1)
            else -> MuscleLoads(legs = 1, impact = 0, cardio = 0, globalFatigue = 0)
        }
    }

    private fun calculateHikingLoads(durationMinutes: Int, elevationM: Int?): MuscleLoads {
        val elev = elevationM ?: 300
        val legLoad = if (elev > 600 || durationMinutes > 180) 4 else 3
        return MuscleLoads(
            legs = legLoad,
            cardio = 3,
            core = 2,
            impact = 2,
            globalFatigue = if (durationMinutes > 180) 4 else 2
        )
    }

    private fun calculateKayakLoads(durationMinutes: Int, intensity: ActivityIntensity): MuscleLoads {
        return MuscleLoads(
            back = 4,
            shoulders = 4,
            arms = 3,
            core = 3,
            cardio = 3,
            globalFatigue = if (durationMinutes > 90) 3 else 2
        )
    }
}
