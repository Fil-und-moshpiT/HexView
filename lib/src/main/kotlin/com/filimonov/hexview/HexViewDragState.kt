package com.filimonov.hexview

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

/**
 * State of hex view that holds dragged value.
 */
internal class HexViewDragState(private val onDragEndPosition: () -> Vec2i) {
    var dragged = mutableStateOf(Vec2i())
        private set

    val dragModifier: Modifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            onDragCancel = onDragEnd
        )
    }

    private val onDrag: (PointerInputChange, Offset) -> Unit = { change, dragAmount ->
        change.consume()
        dragged.value = dragged.value + dragAmount.toVec2i()
    }

    private val onDragEnd: () -> Unit = {
        val newDragged = onDragEndPosition.invoke()

        ValueAnimator.ofObject(DefaultDragEvaluator, Vec2(dragged.value), Vec2(newDragged)).apply {
            duration = 200L
            interpolator = LinearInterpolator()

            addUpdateListener {
                val animated = Vec2i(it.animatedValue as Vec2)
                if (dragged.value != animated) {
                    dragged.value = animated
                }
            }
        }.also { it.start() }
    }
}

private fun Offset.toVec2i() = Vec2i(x, y)

private val DefaultDragEvaluator = TypeEvaluator<Vec2> { fraction, startValue, endValue -> startValue + (endValue - startValue) * fraction }
