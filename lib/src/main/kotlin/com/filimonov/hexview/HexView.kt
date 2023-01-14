package com.filimonov.hexview

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeLayoutState
import androidx.compose.ui.platform.LocalDensity
import glm_.vec2.Vec2i
import kotlin.math.roundToInt

/*
    todo:
        try to cache hex positions
        check center hex calculation on drag end
        +add custom subcompose state
        +add composables caching
 */

@Suppress("unused")
@Composable
fun <T> HexView(
    modifier: Modifier = Modifier,
    orientation: Orientation,
    data: List<T>,
    size: Int,
    content: @Composable (T) -> Unit,
) {
    // create map
    val map = remember(data, size) { HexMap(size, data) }

    BoxWithConstraints(modifier = modifier) {
        val width = with(LocalDensity.current) { maxWidth.roundToPx() }
        val height = with(LocalDensity.current) { maxHeight.roundToPx() }

        // create layout helper
        val layoutHelper = remember(width, height, map, orientation) {
            HexViewLayoutHelper(mapSize = map.size, layoutSize = Vec2i(width, height), orientation = orientation)
        }

        val hexContainer = remember(map, layoutHelper) {
            HexContainer(center = map.center, map.getLayoutCorners(layoutHelper.getTopStartHex(), orientation))
        }

        val dragState = remember(map, layoutHelper, hexContainer) {
            HexViewDragState { layoutHelper.center - layoutHelper.getPosition(hexContainer.center) }
        }

        val itemContentFactory = remember { HexViewItemContentFactory(content) }

        val subcomposeLayoutState = remember(map) { SubcomposeLayoutState(HexViewSlotReusePolicy(map)) }

        SubcomposeLayout(
            state = subcomposeLayoutState,
            modifier = Modifier.fillMaxSize().then(dragState.dragModifier)
        ) {
            layout(width, height) {
                val offset by dragState.dragged
                hexContainer.setCenter(layoutHelper.getCenterHex(-offset))

                val hexRawPositions = hexContainer.offsets.associateWith { layoutHelper.getPosition(it) + offset }

                val hexSizes = hexRawPositions.mapValues {
                    (layoutHelper.hexSize * layoutHelper.getSizeCoefficient(it.value)).roundToInt()
                }

                val hexPositions = hexRawPositions.mapValues {
                    val hex = it.key
                    val position = it.value
                    position - (hexSizes[hex]!! / 2)
                }


                hexContainer.offsets.forEach { hex ->
                    val position = hexPositions[hex]!!
                    val hexSize = hexSizes[hex]!!
                    val localConstraints = constraints.copy(hexSize, hexSize, hexSize, hexSize)

                    val slotId = hex - hexContainer.center // slot id - normalized hex
                    subcompose(slotId) { itemContentFactory.getComposable(map.getData(hex)).invoke() }
                        .map { it.measure(localConstraints) }
                        .forEach { it.place(position.x, position.y) }
                }
            }
        }
    }
}
