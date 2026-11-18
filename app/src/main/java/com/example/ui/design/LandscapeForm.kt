package com.example.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppDesign

// -----------------------------------------------------------------------------
// Landscape form factor.
//
// In portrait the app is a single column you scroll. In landscape a phone gives
// you ~844x390 dp: plenty of width, very little height. The rule this file
// encodes is that the extra width must become *simultaneous zones*, never a
// centred portrait column with huge margins.
//
// Everything here is additive: portrait keeps rendering exactly as before.
// -----------------------------------------------------------------------------

/** Height below which a landscape phone is considered "short" (the common case). */
private val ShortLandscapeHeight = 500.dp

/** Width from which a second pane is worth its gutter. */
private val TwoPaneMinWidth = 600.dp

/** Width from which a third pane is worth its gutter. */
private val ThreePaneMinWidth = 840.dp

/**
 * The single source of truth for landscape decisions. Screens read this instead
 * of poking at [LocalConfiguration] themselves, so the breakpoints stay in one
 * place and the portrait path is never accidentally changed.
 */
@Immutable
data class LandscapeForm(
    val widthDp: Dp,
    val heightDp: Dp,
    val isLandscape: Boolean,
    val isShortLandscape: Boolean,
    val panes: Int
) {
    /** True when the screen should use the two/three-zone landscape layout. */
    val useLandscapeLayout: Boolean get() = isLandscape && panes >= 2

    /** Header budget: a landscape header must never eat the reading pane. */
    val headerHeight: Dp
        get() = when {
            !isLandscape -> 0.dp // portrait keeps its illustrated header
            heightDp < 380.dp -> 44.dp
            else -> 52.dp
        }

    /** Gutter shrinks in landscape so the panes get the width back. */
    val gutter: Dp get() = if (isLandscape) 12.dp else 20.dp

    /**
     * The landscape-phone branch shared by the redesigned areas (the Rosary
     * precedent): wide enough for two panes, short enough that the portrait
     * column cannot work, tall enough to still read (844x390, 915x412, 740x360).
     * Tablets in landscape (height >= 480) keep the portrait/tablet path.
     */
    val isCompactPhone: Boolean
        get() = widthDp >= 600.dp && heightDp < 480.dp && heightDp >= 340.dp
}

@Composable
fun rememberLandscapeForm(): LandscapeForm {
    val config = LocalConfiguration.current
    val width = config.screenWidthDp.dp
    val height = config.screenHeightDp.dp
    return remember(width, height) {
        val landscape = width > height
        val panes = when {
            !landscape -> 1
            width >= ThreePaneMinWidth -> 3
            width >= TwoPaneMinWidth -> 2
            else -> 1
        }
        LandscapeForm(
            widthDp = width,
            heightDp = height,
            isLandscape = landscape,
            isShortLandscape = landscape && height < ShortLandscapeHeight,
            panes = panes
        )
    }
}

/** Tokens shared by every landscape pane, so areas cannot drift apart. */
object LandscapeMetrics {
    val railWidth = 76.dp
    val railItemHeight = 56.dp
    val paneMinWidth = 260.dp
    val paneGap = 12.dp
    val panePadding = 14.dp
    val readingMaxWidth = 640.dp
    val touchTarget = 44.dp
}

/**
 * Hairline between panes. Uses the same separator role as the rest of Lumen so
 * the split reads as part of the design, not as a debug divider.
 */
@Composable
fun PaneDivider(modifier: Modifier = Modifier) {
    val design = LocalAppDesign.current
    Box(
        modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(design.palette.separator)
    )
}

/**
 * Compact replacement for the illustrated screen header in landscape.
 * One line: title on the left, optional actions on the right. No hero art, no
 * three-line stack — the height belongs to the content.
 */
@Composable
fun CompactHeader(
    title: String,
    form: LandscapeForm,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {}
) {
    val design = LocalAppDesign.current
    Row(
        modifier
            .fillMaxWidth()
            .height(form.headerHeight)
            .padding(horizontal = LandscapeMetrics.panePadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            androidx.compose.material3.Text(
                text = title,
                color = design.palette.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (subtitle != null && form.heightDp >= 380.dp) {
                androidx.compose.material3.Text(
                    text = subtitle,
                    color = design.palette.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
        actions()
    }
}

/**
 * Two-zone landscape scaffold: a fixed left pane (index, beads, controls) and a
 * reading pane that owns the only vertical scroll of the screen.
 *
 * [leftWidth] is a fraction of the available width so the reading pane keeps a
 * sane measure on both 740 dp and 915 dp phones.
 */
@Composable
fun LandscapeTwoPane(
    form: LandscapeForm,
    modifier: Modifier = Modifier,
    leftWidthFraction: Float = 0.34f,
    left: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    Row(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(leftWidthFraction)
                .widthIn(min = LandscapeMetrics.paneMinWidth)
        ) { left() }
        PaneDivider()
        Box(
            Modifier
                .fillMaxHeight()
                .weight(1f)
        ) { right() }
    }
}

/**
 * Three-zone variant for the widest phones: rail, index, reading.
 * The rail is optional so areas without navigation can pass an empty slot.
 */
@Composable
fun LandscapeThreePane(
    modifier: Modifier = Modifier,
    railWidth: Dp = LandscapeMetrics.railWidth,
    indexWidthFraction: Float = 0.30f,
    rail: @Composable () -> Unit = {},
    index: @Composable () -> Unit,
    reading: @Composable () -> Unit
) {
    Row(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxHeight().width(railWidth)) { rail() }
        PaneDivider()
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(indexWidthFraction)
                .widthIn(min = LandscapeMetrics.paneMinWidth)
        ) { index() }
        PaneDivider()
        Box(Modifier.fillMaxHeight().weight(1f)) { reading() }
    }
}

/**
 * A pane surface: Lumen glass card, concentric radius, its own padding.
 * Areas use this so every pane in the app has the same skin.
 */
@Composable
fun PaneSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(LandscapeMetrics.panePadding),
    content: @Composable () -> Unit
) {
    val design = LocalAppDesign.current
    Box(
        modifier
            .fillMaxSize()
            .padding(LandscapeMetrics.paneGap / 2)
            .clip(RoundedCornerShape(24.dp))
            .background(design.palette.groupedSurface)
            .padding(contentPadding)
    ) { content() }
}

/** Vertical spacer sized for the short landscape budget. */
@Composable
fun LandscapeGap(form: LandscapeForm) {
    Spacer(Modifier.height(if (form.isShortLandscape) 8.dp else 12.dp))
}
