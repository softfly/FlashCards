package pl.softfly.flashcards.ui.kt.theme

import android.view.View
import android.view.Window
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

val DarkColorPalette = darkColors(
    primary = Grey1000,
    primaryVariant = Grey1000,
    onPrimary = Color.White,
    onSecondary = Color.White,
    background = Color.Black,
)
val LightColorPalette = lightColors(
    primary = Purple900
)

@Composable
fun FlashCardsTheme(
    isDarkTheme: Boolean,
    window: Window? = null,
    content: @Composable () -> Unit,
) {
    window?.statusBarColor =
        if (isDarkTheme) DarkColorPalette.primary.toArgb() else LightColorPalette.primary.toArgb()
    MaterialTheme(
        colors = if (isDarkTheme) DarkColorPalette else LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}

@Composable
fun FlashCardsAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, "Back")
        }
    },
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primary,
    contentColor: Color = contentColorFor(backgroundColor),//MaterialTheme.colors.onPrimary
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = elevation
    )
}