package com.abpvt.newsapp.ui.news.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.abpvt.newsapp.ui.theme.ShimmerBase
import com.abpvt.newsapp.ui.theme.ShimmerHighlight

@Composable
fun FeedSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "feed_skeleton")
    val x by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "feed_skeleton_x"
    )
    val brush = Brush.linearGradient(
        listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        Offset(x, 0f),
        Offset(x + 380f, 300f)
    )
    Column(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(3) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(brush, RoundedCornerShape(20.dp))
                    .padding(bottom = 16.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(180.dp).background(brush, RoundedCornerShape(20.dp)))
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.width(90.dp).height(18.dp).background(brush, RoundedCornerShape(9.dp)))
                    Box(Modifier.width(70.dp).height(18.dp).background(brush, RoundedCornerShape(9.dp)))
                }
                Box(Modifier.fillMaxWidth(0.9f).height(24.dp).padding(horizontal = 16.dp).background(brush, RoundedCornerShape(8.dp)))
            }
        }
    }
}
