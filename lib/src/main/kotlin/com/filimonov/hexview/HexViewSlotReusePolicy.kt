package com.filimonov.hexview

import androidx.compose.ui.layout.SubcomposeSlotReusePolicy

internal class HexViewSlotReusePolicy<D>(private val map: HexMap<D>) : SubcomposeSlotReusePolicy {
    override fun getSlotsToRetain(slotIds: SubcomposeSlotReusePolicy.SlotIdsSet) {
        // retain all slots because every item should be composed
    }

    override fun areCompatible(slotId: Any?, reusableSlotId: Any?): Boolean {
        val first = if (slotId != null)  map.getData(slotId as Hex) else null
        val second = if (reusableSlotId != null)  map.getData(reusableSlotId as Hex) else null

        return first == second
    }
}
