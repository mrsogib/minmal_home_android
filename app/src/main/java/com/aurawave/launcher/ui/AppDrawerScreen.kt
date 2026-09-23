// package com.aurawave.launcher.ui

// import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.PaddingValues
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.lazy.items
// import androidx.compose.foundation.text.KeyboardOptions
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.OutlinedTextField
// import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.remember
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.focus.FocusRequester
// import androidx.compose.ui.focus.focusRequester
// import androidx.compose.ui.platform.LocalFocusManager
// import androidx.compose.ui.text.input.ImeAction
// import androidx.compose.ui.unit.dp
// import com.aurawave.launcher.data.AppInfo
// import com.aurawave.launcher.ui.components.AppListItem
// import androidx.compose.runtime.LaunchedEffect

// /**
//  * Full app drawer: search field pinned at top (auto-focused so the keyboard
//  * pops immediately — you can start typing an app name the instant it opens),
//  * filtered results below.
//  */
// @Composable
// fun AppDrawerScreen(
//     apps: List<AppInfo>,
//     query: String,
//     onQueryChange: (String) -> Unit,
//     onLaunch: (AppInfo) -> Unit,
//     onHide: (AppInfo) -> Unit,
//     onAppInfo: (AppInfo) -> Unit,
//     onUninstall: (AppInfo) -> Unit
// ) {
//     val focusRequester = remember { FocusRequester() }
//     val focusManager = LocalFocusManager.current

//     LaunchedEffect(Unit) {
//         focusRequester.requestFocus()
//     }

//     Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
//         OutlinedTextField(
//             value = query,
//             onValueChange = onQueryChange,
//             singleLine = true,
//             placeholder = { Text("Search apps…") },
//             keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//             modifier = Modifier
//                 .fillMaxWidth()
//                 .padding(horizontal = 20.dp, vertical = 8.dp)
//                 .focusRequester(focusRequester)
//         )

//         if (apps.isEmpty()) {
//             Text(
//                 text = "No apps found",
//                 style = MaterialTheme.typography.bodyMedium,
//                 modifier = Modifier.padding(24.dp)
//             )
//         }

//         LazyColumn(contentPadding = PaddingValues(bottom = 48.dp)) {
//             items(apps, key = { it.componentKey }) { app ->
//                 AppListItem(
//                     app = app,
//                     onClick = { onLaunch(app) },
//                     onHide = { onHide(app) },
//                     onAppInfo = { onAppInfo(app) },
//                     onUninstall = { onUninstall(app) }
//                 )
//             }
//         }
//     }
// }












package com.aurawave.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurawave.launcher.data.AppInfo

@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    initialLetter: Char?,
    onAppClick: (String) -> Unit,
    onBack: () -> Unit,
    onRenameApp: (String, String) -> Unit
) {
    BackHandler { onBack() }

    val listState = rememberLazyListState()
    var renameTargetPkg by remember { mutableStateOf<Pair<String, String>?>(null) }
    var searchFilter by remember { mutableStateOf("") }

    val filteredApps = remember(apps, searchFilter) {
        if (searchFilter.isBlank()) apps else apps.filter { it.label.contains(searchFilter, ignoreCase = true) }
    }

    LaunchedEffect(initialLetter, filteredApps) {
        if (initialLetter != null) {
            val index = filteredApps.indexOfFirst {
                it.label.firstOrNull()?.equals(initialLetter, ignoreCase = true) == true
            }
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchFilter,
                onValueChange = { searchFilter = it },
                placeholder = { Text("Search apps...", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(filteredApps) { _, app ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppClick(app.packageName) }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = app.label,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Rename",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .clickable { renameTargetPkg = Pair(app.packageName, app.label) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        renameTargetPkg?.let { (pkg, currentLabel) ->
            var textValue by remember { mutableStateOf(currentLabel) }
            AlertDialog(
                onDismissRequest = { renameTargetPkg = null },
                title = { Text("Rename App") },
                text = {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onRenameApp(pkg, textValue)
                        renameTargetPkg = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameTargetPkg = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
