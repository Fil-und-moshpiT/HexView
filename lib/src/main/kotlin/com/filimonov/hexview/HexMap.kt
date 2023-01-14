package com.filimonov.hexview

/**
 * Represents hexagon of [Hex]es with size [size]. Main hexagon wrapped
 * with six similar hexagons in order to provide infinite drag.
 * @param size Size of hexagon
 * @param elements Elements that fill hexagon counterclockwise
 */
internal class HexMap<T>(val size: Int, private val elements: List<T>) {
    val center = Hex(0, 0, 0)
    private val hexes: List<Hex>
        get() = hexToIds.keys.toList()

    /**
     * Stores unique id for each hex. Minimal value = 0. Maximal value = 3size^2 + 3size + 1.
     * @see [hexMod]
     */
    private val hexToIds = LinkedHashMap<Hex, Int>()

    /**
     * Stores data for each id
     */
    private val idToElements = mutableMapOf<Int, T>()

    init {
        reorder()
    }

    fun getNormalizedData(hex: Hex): T {
        val hexMod = hexMod(hex)
        return idToElements[hexMod]!!
    }

    private fun hexMod(hex: Hex): Int {
        val a = 3 * size * size + 3 * size + 1
        val s = 3 * size + 2
        // q r s
        return mod(hex.r + s * hex.q, a)
    }

    private fun mod(a: Int, b: Int) = ((a % b) + b) % b

    private fun reorder() {
        // clear elements
        hexToIds.clear()

        // and fill spirally
        if (elements.isEmpty()) return

        spiralMove(center).forEachIndexed { index, hex ->
            val element = elements[index.mod(elements.indices.last)]
            val id = hexMod(hex)

            hexToIds[hex] = id
            idToElements[id] = element
        }
    }

    private fun spiralMove(center: Hex) =
        buildList {
            add(center)

            (1..this@HexMap.size).forEach { radius -> addAll(ringMove(center, radius)) }
        }

    private fun ringMove(center: Hex, radius: Int): List<Hex> =
        buildList {
            var currentHex = center + (Hex.getDirection(4) * radius)

            (0 until 6).forEach { direction ->
                (0 until radius).forEach { _ ->
                    add(currentHex)
                    currentHex = currentHex.getNeighbour(direction)
                }
            }
        }

    fun getLayoutCorners(topStart: Hex, orientation: Orientation): LayoutCorners {
        return when (orientation) {
            is Orientation.FlatTop -> {
                // bottom start = reflect q
                // top end = center - bottomStart
                // bottom end = center - topStart

                // reflect topStart by q-axis
                val bottomStart = Hex(topStart.q, topStart.s, topStart.r)
                val topEnd = center - bottomStart
                val bottomEnd = center - topStart

                LayoutCorners(
                    topStart = topStart,
                    topEnd = topEnd,
                    bottomStart = bottomStart,
                    bottomEnd = bottomEnd
                )
            }
            is Orientation.PointyTop -> {
                // top end = reflect r
                // bottom start = center - topEnd
                // bottom end = center - topStart

                // reflect topStart by r-axis
                val topEnd = Hex(topStart.s, topStart.r, topStart.q)
                val bottomStart = center - topEnd
                val bottomEnd = center - topStart

                LayoutCorners(
                    topStart = topStart,
                    topEnd = topEnd,
                    bottomStart = bottomStart,
                    bottomEnd = bottomEnd
                )
            }
        }
    }

    data class LayoutCorners(
        val topStart: Hex,
        val topEnd: Hex,
        val bottomStart: Hex,
        val bottomEnd: Hex,
    )
}
