/*
 * Copyright 2020 The Android Open Source Project
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

@file:OptIn(ExperimentalLayoutApi::class)

package com.google.accompanist.sample.insets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBarContent
import com.google.accompanist.insets.ui.TopAppBarSurface
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class InsetsBasicSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Update the system bars to be translucent
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }

            AccompanistSampleTheme {
                InsetsBasics()
            }
        }
    }
}

@Composable
internal fun InsetsBasics() {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val horizontalContentPadding = WindowInsets.systemBars.let {
        WindowInsets(
            left = it.getLeft(density, layoutDirection),
            right = it.getRight(density, layoutDirection)
        )
    }.asPaddingValues()

    Scaffold(
        topBar = {
            // We use TopAppBar from accompanist-insets-ui which allows us to provide
            // content padding matching the system bars insets.
            TopAppBarSurface(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBarContent(
                    title = { Text(stringResource(R.string.insets_title_list)) },
                    modifier = Modifier.windowInsetsPadding(
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
        },
        bottomBar = {
            // We add a spacer as a bottom bar, which is the same height as
            // the navigation bar
            Spacer(
                Modifier
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .fillMaxWidth())
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Face icon"
                )
            }
        },
        contentPadding = horizontalContentPadding
    ) { contentPadding ->
        // We apply the contentPadding passed to us from the Scaffold
        Box(Modifier.padding(contentPadding)) {
            // content
        }
    }
}
