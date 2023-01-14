package com.filimonov.hexview

/**
 * Contain offsets from center hex for hexes that fits on screen
 */
internal class HexContainer(
    center: Hex,
    corners: HexMap.LayoutCorners
) {
    var center: Hex = center
        private set

    var offsets = emptyList<Hex>()
        private set

    init {
        offsets = buildList {
            val topToBottomHexes = buildList {
                val topStart = FractionalHex(
                    corners.topStart.q + 1e-6f,
                    corners.topStart.r + 1e-6f,
                    corners.topStart.s - 2e-6f
                )

                val topEnd = FractionalHex(
                    corners.topEnd.q + 1e-6f,
                    corners.topEnd.r + 1e-6f,
                    corners.topEnd.s - 2e-6f
                )

                val bottomStart = FractionalHex(
                    corners.bottomStart.q + 1e-6f,
                    corners.bottomStart.r + 1e-6f,
                    corners.bottomStart.s - 2e-6f
                )

                val bottomEnd = FractionalHex(
                    corners.bottomEnd.q + 1e-6f,
                    corners.bottomEnd.r + 1e-6f,
                    corners.bottomEnd.s - 2e-6f
                )

                val distance = kotlin.math.max(
                    corners.topStart.distanceTo(corners.topEnd),
                    corners.bottomStart.distanceTo(corners.bottomEnd)
                )

                (0..distance).forEach {
                    val top = hexLerp(topStart, topEnd, (1f / distance) * it)
                    val bottom = hexLerp(bottomStart, bottomEnd, (1f / distance) * it)

                    add(top to bottom)
                }
            }

            topToBottomHexes.forEach {
                val top = it.first
                val bottom = it.second

                val fractionalTop = FractionalHex(top.q + 1e-6f, top.r + 1e-6f, top.s - 2e-6f)
                val fractionalBottom = FractionalHex(bottom.q + 1e-6f, bottom.r + 1e-6f, bottom.s - 2e-6f)

                val distance = top.distanceTo(bottom)
                (0..distance).forEach { i -> add(hexLerp(fractionalTop, fractionalBottom, (1f / distance) * i)) }
            }

            distinct()
        }
    }

    fun setCenter(hex: Hex) {
        if (center != hex) {
            onCenterChanged(center - hex)

            center = hex
        }
    }

    private fun onCenterChanged(hex: Hex) {
//        val iterator = offsets.listIterator()
//
//        while (iterator.hasNext()) {
//            iterator.set(iterator.next() - hex)
//        }

        offsets = offsets.map { it - hex }
    }

    // LERP - linear interpolation
    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    private fun hexLerp(a: FractionalHex, b: FractionalHex, t: Float): Hex {
        val fq = lerp(a.q, b.q, t)
        val fr = lerp(a.r, b.r, t)
        val fs = lerp(a.s, b.s, t)

        return FractionalHex(fq, fr,fs).toHex()
    }
}
