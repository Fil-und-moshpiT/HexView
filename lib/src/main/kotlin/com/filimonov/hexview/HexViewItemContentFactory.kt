package com.filimonov.hexview

import androidx.compose.runtime.Composable

/**
 * Class that caches composables for each element.
 * Elements can be obtained with [getComposable] by data.
 */
internal class HexViewItemContentFactory<D>(
    private val composableFactory: @Composable (D) -> Unit
) {
    init { println("allocated [$this]") }
    private val composablesCache = mutableMapOf<D, CachedComposableItem>()

    fun getComposable(data: D): @Composable () -> Unit {
        val cachedComposable = composablesCache[data]

        return cachedComposable?.composable
            ?: CachedComposableItem(data).also { composablesCache[data] = it }.composable
    }

    /**
     * Caches composable by data
     */
    private inner class CachedComposableItem(private val data: D) {
        private var _composable: (@Composable () -> Unit)? = null
        val composable: (@Composable () -> Unit)
            get() = _composable ?: createComposable().also { _composable = it }

        private fun createComposable() = @Composable {
            composableFactory.invoke(data)

            // clear composable when disposed to not leak RecomposeScopes
//            DisposableEffect(key) { onDispose {
//                _content = null
//                println("disposed for $key")
//            } }
        }
    }
}
