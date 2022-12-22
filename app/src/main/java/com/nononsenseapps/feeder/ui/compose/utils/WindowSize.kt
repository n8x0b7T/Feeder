package com.nononsenseapps.feeder.ui.compose.utils

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator
import com.nononsenseapps.feeder.util.logDebug

/**
 * Opinionated set of viewport breakpoints
 *     - CompactTall: Most phones in portrait mode
 *     - CompactShort: Most phones in landscape - and small phones in portrait too
 *     - Medium: Most foldables and tablets in portrait mode
 *     - Expanded: Most tablets in landscape mode
 *
 * More info: https://material.io/archive/guidelines/layout/responsive-ui.html
 */
enum class WindowSize {
    CompactTall,
    CompactShort,
    CompactWide,
    Medium,
    Expanded
}

val localWindowSize: ProvidableCompositionLocal<WindowSize> =
    compositionLocalOf { error("Missing WindowSize container!") }

@Composable
fun LocalWindowSize(): WindowSize = localWindowSize.current

@Composable
fun Activity.withWindowSize(content: @Composable () -> Unit) {
    // Get the size (in pixels) of the window
    val windowSize = rememberWindowSize()

    // Convert the window size to [Dp]
    val windowDpSize = with(LocalDensity.current) {
        windowSize.toDpSize()
    }

    // Calculate the window size class
    val windowSizeclass = getWindowSizeClass(windowDpSize)

    CompositionLocalProvider(localWindowSize provides windowSizeclass) {
        content()
    }
}

/**
 * Remembers the [WindowSize] class for the window corresponding to the current window metrics.
 */
@Composable
fun Activity.rememberWindowSizeClass(): WindowSize {
    // Get the size (in pixels) of the window
    val windowSize = rememberWindowSize()

    // Convert the window size to [Dp]
    val windowDpSize = with(LocalDensity.current) {
        windowSize.toDpSize()
    }

    // Calculate the window size class
    return getWindowSizeClass(windowDpSize)
}

/**
 * Remembers the [Size] in pixels of the window corresponding to the current window metrics.
 */
@Composable
private fun Activity.rememberWindowSize(): Size {
    val configuration = LocalConfiguration.current
    // WindowMetricsCalculator implicitly depends on the configuration through the activity,
    // so re-calculate it upon changes.
    val windowMetrics = remember(configuration) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    }
    return windowMetrics.bounds.toComposeRect().size
}

/*
Formula: dp = px * 160 / DPI

Mi A1: 762dp
Pixel 2: 731dp
Pixel 5: 850dp
S22: 880dp
S22+: 952dp (actual 832dp)
S22 Ultra: 988dp
*/
private val shortHeightLimit = 800.dp

/**
 * Partitions a [DpSize] into a enumerated [WindowSize] class.
 */
@Composable
fun getWindowSizeClass(windowDpSize: DpSize): WindowSize {
    val configuration = LocalConfiguration.current
    return when {
        windowDpSize.width < 0.dp -> throw IllegalArgumentException("Dp value cannot be negative")
        windowDpSize.width < 600.dp -> {
            when {
                windowDpSize.height < shortHeightLimit -> {
                    when (configuration.orientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> WindowSize.CompactWide
                        else -> WindowSize.CompactShort
                    }
                }
                else -> WindowSize.CompactTall
            }
        }
        windowDpSize.width < 840.dp -> {
            WindowSize.Medium
        }
        else -> WindowSize.Expanded
    }.also {
        logDebug("FEEDER_WINDOWSIZE", "getWindowSizeClass($windowDpSize) -> $it")
    }
}

enum class ScreenType {
    DUAL,
    SINGLE,
}

fun getScreenType(windowSize: WindowSize) =
    when (windowSize) {
        WindowSize.CompactTall, WindowSize.CompactShort -> ScreenType.SINGLE
        WindowSize.CompactWide, WindowSize.Medium, WindowSize.Expanded -> ScreenType.DUAL
    }
