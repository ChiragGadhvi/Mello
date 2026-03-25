package com.chirag.mello.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.chirag.mello.R
import com.chirag.mello.ui.theme.Background

@Composable
fun MelloBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen background image
        Image(
            painter = painterResource(id = R.drawable.bg_img),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background.copy(alpha = 0.72f))
        )
        // Page content
        content()
    }
}
