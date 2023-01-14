package com.filimonov.hexview

import glm_.d
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Class that helps to work with layout: get position of hex, get hex by position, calculate sizes, etc.
 *
 * @param mapSize Size of [HexMap]
 * @param layoutSize Width and height of layout
 * @param orientation Orientation of layout
 */
internal class HexViewLayoutHelper(
    mapSize: Int,
    val layoutSize: Vec2i,
    private val orientation: Orientation
){
    val center = layoutSize / 2

    /**
     * Hex size for displaying
     */
    val hexHalfSize = orientation.getHexInnerRadius(max(layoutSize.x, layoutSize.y), mapSize - 1)

    /**
     * Hex size for internal computing
     */
    private val hexRadius = orientation.getHexOuterRadius(max(layoutSize.x, layoutSize.y), mapSize - 1)

    private val distanceToCenter = distance(center, Vec2i())

    fun getPosition(hex: Hex): Vec2i = Vec2i(center + orientation.matrix * Vec2(hex.q, hex.r) * hexRadius)

    fun getTopStartHex() = getHexFromPoint(Vec2i() - hexRadius * 2)

    fun getCenterHex(offset: Vec2i): Hex = getHexFromPoint(center + offset)

    fun getSizeCoefficient(hex: Hex, offset: Vec2i): Float {
        val hexDistance = distance(center, offset + getPosition(hex))
        val result = IntRange(100, 25).scaleFor(hexDistance, distanceToCenter) / 100f

        return if (result > 0f) result else 0f
    }

    private fun getHexFromPoint(point: Vec2i): Hex =
        with(
            // convert point to fractional hex
            orientation.inverse * Vec2((point - center) / hexRadius)
        ) {
            FractionalHex(x, y, -x - y).toHex()
        }
}

private fun distance(a: Vec2i, b: Vec2i): Int = sqrt(((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)).d).i

private fun IntRange.scaleFor(current: Int, maximum: Int): Float {
    val rangeMin = this.first.toFloat()
    val rangeMax = this.last.toFloat()

    return (((rangeMax - rangeMin) * current) / maximum.toFloat()) + rangeMin
}
