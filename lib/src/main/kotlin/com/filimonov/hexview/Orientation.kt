package com.filimonov.hexview

import glm_.mat2x2.Mat2
import kotlin.math.roundToInt
import kotlin.math.sqrt

// todo: change get radius functions returns from Vec2 to Float
sealed class Orientation(
    val matrix: Mat2,
    val inverse: Mat2 = matrix.inverse()
) {
    /**
     * return outer radius for hex
     */
    abstract fun getHexOuterRadius(layoutSize: Int, count: Int): Int

    /**
     * return inner radius for hex
     */
    fun getHexInnerRadius(layoutSize: Int, count: Int): Int = (getHexOuterRadius(layoutSize, count) * sqrt(3f) / 2).roundToInt()

    protected fun innerSize(layoutSize: Int, count: Int): Int {
        val halves = (count * 2 + 1) * 2f
        val halfSize = layoutSize / halves

        return (halfSize / sqrt(3.0) * 2).roundToInt()
    }

    protected fun outerSize(layoutSize: Int, count: Int): Int {
        val quarters = 4 + (3 * count * 2).toFloat()
        val quarterSize = layoutSize / quarters

        return (quarterSize * 2).roundToInt()
    }

    object FlatTop : Orientation(matrix = Mat2(3.0 / 2.0, sqrt(3.0) / 2.0, 0.0, sqrt(3.0))) {
        override fun getHexOuterRadius(layoutSize: Int, count: Int) = outerSize(layoutSize, count)
    }

    object PointyTop : Orientation(matrix = Mat2(sqrt(3.0), 0.0, sqrt(3.0) / 2.0, 3.0 / 2.0)) {
        override fun getHexOuterRadius(layoutSize: Int, count: Int) = innerSize(layoutSize, count)
    }
}
