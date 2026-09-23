package com.aurawave.launcher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.aurawave.launcher.data.AppInfo
import com.aurawave.launcher.ui.components.AppListItem
import androidx.compose.runtime.LaunchedEffect

/**
 * Full app drawer: search field pinned at top (auto-focused so the keyboard
 * pops immediately — you can start typing an app name the instant it opens),
 * filtered results below.
 */
@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    query: String,
    onQueryChange: (String) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onHide: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("Search apps…") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .focusRequester(focusRequester)
        )

        if (apps.isEmpty()) {
            Text(
                text = "No apps found",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp)
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 48.dp)) {
            items(apps, key = { it.componentKey }) { app ->
                AppListItem(
                    app = app,
                    onClick = { onLaunch(app) },
                    onHide = { onHide(app) },
                    onAppInfo = { onAppInfo(app) },
                    onUninstall = { onUninstall(app) }
                )
            }
        }
    }
}
