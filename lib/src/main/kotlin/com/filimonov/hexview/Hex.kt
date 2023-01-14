package com.filimonov.hexview

import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Class that store cube coordinates and provides methods for interaction with another hexes
 */
// todo: change to Vec3i?
internal data class Hex constructor(
    val q: Int,
    val r: Int,
    val s: Int
) {
    constructor(q: Int, r: Int) : this(q, r, -q - r) // axial constructor

    init { require(q + r + s == 0) { "Sum of hex coordinates should be equal zero! Found: q=$q, r=$r, s=$s" } }

    operator fun plus(other: Hex) = Hex(q + other.q, r + other.r, s + other.s)

    operator fun minus(other: Hex) = Hex(q - other.q, r - other.r, s - other.s)

    operator fun times(k: Int) = Hex(q * k, r * k, s * k)

    private fun length(): Int = (abs(q) + abs(r) + abs(s)) / 2

    fun distanceTo(other: Hex): Int = (this - other).length()

    fun getNeighbour(direction: Int) = this + getDirection(direction)

    companion object {
        private val directions = arrayOf(
            Hex(+1, +0),
            Hex(+1, -1),
            Hex(+0, -1),
            Hex(-1, +0),
            Hex(-1, +1),
            Hex(+0, +1)
        )

        fun getDirection(n: Int): Hex {
            require(n in 0..5) { "Invalid hex direction" }
            return directions[n]
        }
    }

    override fun toString(): String = "q=$q, r=$r, s=$s"
}

internal data class FractionalHex(
    val q: Float,
    val r: Float,
    val s: Float
) {
    fun toHex(): Hex {
        var q = round(this.q).roundToInt()
        var r = round(this.r).roundToInt()
        var s = round(this.s).roundToInt()

        val dq = abs(q - this.q)
        val dr = abs(r - this.r)
        val ds = abs(s - this.s)

        if (dq > dr && dq > ds) {
            q = -r - s
        } else if (dr > ds) {
            r = -q - s
        } else {
            s = -q - r
        }

        return Hex(q, r, s)
    }
}
