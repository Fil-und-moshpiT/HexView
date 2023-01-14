package com.filimonov.hexview

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import glm_.vec2.Vec2i
import kotlin.math.roundToInt

/*
    todo:
        +1 define hexes that will fill all screen
        +2 store in hexMap only one big hex
        +3 define central hex
        +4 on drag move hex grid and offset central hex
        +5 on drag end calculate central hex and store it
        +6 normalize hexes which out of hexMap
        +7 add calculating elements size by distance from center

    links:
        https://observablehq.com/@sanderevers/hexagon-tiling-of-an-hexagonal-grid
        https://observablehq.com/@sanderevers/hexmod-representation
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

        SubcomposeLayout(
            modifier = Modifier.fillMaxSize().then(dragState.dragModifier)
        ) {
            layout(width, height) {
                val offset by dragState.dragged
                hexContainer.setCenter(layoutHelper.getCenterHex(-offset))

                val hexSizes = hexContainer.offsets.associateWith {
                    val coefficient = layoutHelper.getSizeCoefficient(it, offset)

                    (layoutHelper.hexHalfSize * coefficient).roundToInt() * 2
                }

                val hexPositions = hexContainer.offsets.associateWith { layoutHelper.getPosition(it) + offset - (hexSizes[it]!! / 2) }

                hexContainer.offsets.forEach { hex ->
                    val position = hexPositions[hex]!!
                    val hexSize = hexSizes[hex]!!
                    val localConstraints = constraints.copy(hexSize, hexSize, hexSize, hexSize)

                    subcompose(hex) { content.invoke(map.getNormalizedData(hex)) }
                        .map { it.measure(localConstraints) }
                        .forEach { it.place(position.x, position.y) }
                }
            }
        }
    }
}
