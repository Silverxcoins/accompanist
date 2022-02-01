/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.accompanist.sample.swiperefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ui.TopAppBarContent
import com.google.accompanist.insets.ui.TopAppBarSurface
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.insets.ListItem
import com.google.accompanist.sample.randomSampleImageUrl
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

class SwipeRefreshContentPaddingSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AccompanistSampleTheme {
                Sample()
            }
        }
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }

@Composable
private fun Sample() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }

    Surface {
        Box(Modifier.fillMaxSize()) {
            // Simulate a fake 2-second 'load'. Ideally this 'refreshing' value would
            // come from a ViewModel or similar
            var refreshing by remember { mutableStateOf(false) }
            LaunchedEffect(refreshing) {
                if (refreshing) {
                    delay(2000)
                    refreshing = false
                }
            }

            AppBarSizeAwareLayout(
                appBarContent = {
                    TopAppBarSurface(
                        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val density = LocalDensity.current
                        val layoutDirection = LocalLayoutDirection.current

                        TopAppBarContent(
                            title = {
                                Text(stringResource(R.string.swiperefresh_title_content_padding))
                            },
                            modifier = Modifier.windowInsetsPadding(
                                // TODO: Make this cleaner
                                //       https://issuetracker.google.com/issues/217768486
                                WindowInsets.systemBars.let {
                                    WindowInsets(
                                        left = it.getLeft(density, layoutDirection),
                                        top = it.getTop(density),
                                        right = it.getRight(density, layoutDirection)
                                    )
                                }
                            )
                        )
                    }
                }
            ) { contentPadding ->
                val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
                val layoutDirection = LocalLayoutDirection.current
                val fullContentPadding = PaddingValues(
                    start = maxOf(
                        contentPadding.calculateStartPadding(layoutDirection),
                        systemBarsPadding.calculateStartPadding(layoutDirection)
                    ),
                    top = maxOf(
                        contentPadding.calculateTopPadding(),
                        systemBarsPadding.calculateTopPadding()
                    ),
                    end = maxOf(
                        contentPadding.calculateEndPadding(layoutDirection),
                        systemBarsPadding.calculateEndPadding(layoutDirection)
                    ),
                    bottom = maxOf(
                        contentPadding.calculateBottomPadding(),
                        systemBarsPadding.calculateBottomPadding()
                    ),
                )

                SwipeRefresh(
                    state = rememberSwipeRefreshState(refreshing),
                    onRefresh = { refreshing = true },
                    // Shift the indicator to match the list content padding
                    indicatorPadding = fullContentPadding,
                    // We want the indicator to draw within the padding
                    clipIndicatorToPadding = false,
                    // Tweak the indicator to scale up/down
                    indicator = { state, refreshTriggerDistance ->
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = refreshTriggerDistance,
                            scale = true
                        )
                    }
                ) {
                    LazyColumn(contentPadding = fullContentPadding) {
                        items(items = listItems) { imageUrl ->
                            ListItem(imageUrl, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBarSizeAwareLayout(
    appBarContent: @Composable () -> Unit,
    mainContent: @Composable (PaddingValues) -> Unit
) {
    val density = LocalDensity.current

    SubcomposeLayout { constraints ->
        val appBarPlaceables = subcompose(SlotsEnum.AppBar, appBarContent).map {
            it.measure(constraints)
        }
        val maxHeight = appBarPlaceables.maxOf { it.measuredHeight }

        val contentPadding = PaddingValues(top = with(density) { maxHeight.toDp() })

        layout(constraints.maxWidth, constraints.maxHeight) {
            subcompose(SlotsEnum.Content) {
                mainContent(contentPadding)
            }.forEach {
                it.measure(constraints).placeRelative(0, 0)
            }
            appBarPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}

enum class SlotsEnum {
    AppBar, Content
}
