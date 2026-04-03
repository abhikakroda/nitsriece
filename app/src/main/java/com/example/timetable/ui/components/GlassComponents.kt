package com.example.timetable.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.timetable.ui.theme.GradientHeader
import com.example.timetable.ui.theme.GradientHeaderDark
import com.example.timetable.ui.theme.GradientMeshDark
import com.example.timetable.ui.theme.GradientMeshLight

/**
 * Clean, minimal card with white background, subtle shadow, and light blue border.
 * Default shape is 16dp for a modern, approachable look.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.12f else 0.05f),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.1f else 0.03f)
            )
            .clip(shape)
            .background(
                if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                else Color.White
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.45f else 0.55f), shape),
        content = content
    )
}

/**
 * "Liquid glass" surface: translucent, slightly blurred (Android 12+), with a soft specular highlight.
 *
 * Note: true iOS-style backdrop blur isn't fully available in Compose across all Android versions.
 * This provides a very close look via translucency + subtle highlight + optional layer blur.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadius: Dp = 18.dp,
    borderAlpha: Float = 0.22f,
    contentAlpha: Float = 0.72f,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassBase = if (dark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val borderColor = if (dark) {
        Color.White.copy(alpha = borderAlpha)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha * 0.8f)
    }
    val highlightColor = MaterialTheme.colorScheme.secondary.copy(alpha = if (dark) 0.06f else 0.08f)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (dark) 0.35f else 0.12f),
                spotColor = Color.Black.copy(alpha = if (dark) 0.35f else 0.10f)
            )
            .clip(shape)
            .then(
                if (Build.VERSION.SDK_INT >= 31) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            blurRadius.toPx(),
                            blurRadius.toPx(),
                            Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                } else {
                    Modifier
                }
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        glassBase.copy(alpha = if (dark) (contentAlpha * 0.58f) else contentAlpha),
                        MaterialTheme.colorScheme.primary.copy(alpha = if (dark) 0.08f else 0.04f)
                    )
                )
            )
            .border(1.dp, borderColor, shape)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (dark) 0.11f else 0.2f),
                            Color.Transparent,
                            highlightColor
                        )
                    ),
                    alpha = 1f,
                    blendMode = BlendMode.SrcOver
                )
            },
        content = content
    )
}

/**
 * Card with a linear gradient background — used for hero/highlight cards.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    colors: List<Color>,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(colors)),
        content = content
    )
}

@Composable
fun DashboardVibeContainer(
    modifier: Modifier = Modifier,
    headerHeight: Dp = 280.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val headerGradient = if (isDark) GradientHeaderDark else GradientHeader
    val mesh = if (isDark) GradientMeshDark else GradientMeshLight
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(mesh))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + 36.dp)
                .background(
                    brush = Brush.verticalGradient(headerGradient),
                    shape = RoundedCornerShape(bottomStart = 38.dp, bottomEnd = 38.dp)
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + 110.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        content()
    }
}
