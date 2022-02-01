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

package com.google.accompanist.sample.insets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.rememberImeNestedScrollConnection
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBarContent
import com.google.accompanist.insets.ui.TopAppBarSurface
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalAnimatedInsets::class)
class ImeAnimationSample : ComponentActivity() {
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
                Sample()
            }
        }
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }

@OptIn(ExperimentalAnimatedInsets::class)
@Composable
private fun Sample() {
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
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBarContent(
                    title = { Text(stringResource(R.string.insets_title_imeanim)) },
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
            Surface(elevation = 1.dp) {
                val text = remember { mutableStateOf(TextFieldValue()) }
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    placeholder = { Text(text = "Watch me animate...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        contentPadding = horizontalContentPadding
    ) { contentPadding ->
        Column {
            // We apply the contentPadding passed to us from the Scaffold
            LazyColumn(
                contentPadding = contentPadding,
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(connection = rememberImeNestedScrollConnection())
            ) {
                items(listItems) { imageUrl ->
                    ListItem(imageUrl, Modifier.fillMaxWidth())
                }
            }
        }
    }
}
