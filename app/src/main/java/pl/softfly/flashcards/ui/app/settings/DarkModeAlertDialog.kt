package pl.softfly.flashcards.ui.app.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.softfly.flashcards.entity.AppConfig
import pl.softfly.flashcards.ui.kt.theme.FlashCardsTheme

@Preview(showBackground = true)
@Composable
fun PreviewDarkModeAlertDialog() {
    val isDarkTheme = true
    FlashCardsTheme(isDarkTheme = isDarkTheme) {
        DarkModeAlertDialog(
            isDarkTheme = isDarkTheme,
            darkModeOptionSelectedS = remember { mutableStateOf(AppConfig.DARK_MODE_OPTIONS[1]) }
        )
    }
}

@Composable
fun DarkModeAlertDialog(
    isDarkTheme: Boolean,
    darkModeOptionSelectedS: State<String?>,
    onSelectDarkMode: (selected: String) -> Unit = {},
    onSaveDarkMode: () -> Unit = {},
    onDismissDarkMode: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { onDismissDarkMode() },
        confirmButton = {
            TextButton(onClick = { onSaveDarkMode() })
            {
                Text(
                    text = "OK",
                    color = if (isDarkTheme) Color.White else MaterialTheme.colors.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissDarkMode() })
            {
                Text(
                    text = "Cancel",
                    color = if (isDarkTheme) Color.White else MaterialTheme.colors.primary
                )
            }
        },
        title = {
            Text(text = "Dark mode", style = MaterialTheme.typography.h6.merge())
        },
        text = {
            // https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#RadioButton(kotlin.Boolean,kotlin.Function0,androidx.compose.ui.Modifier,kotlin.Boolean,androidx.compose.foundation.interaction.MutableInteractionSource,androidx.compose.material.RadioButtonColors)
            Column(Modifier.selectableGroup()) {
                AppConfig.DARK_MODE_OPTIONS.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (text == darkModeOptionSelectedS.value),
                                onClick = { onSelectDarkMode(text) },
                                role = Role.RadioButton
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == darkModeOptionSelectedS.value),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = text,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    )
}