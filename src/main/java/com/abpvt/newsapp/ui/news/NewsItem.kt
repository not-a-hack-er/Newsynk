package com.abpvt.newsapp.ui.news

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewsItem(
    article: Article,
    onArticleClick: () -> Unit,
    onCommentClick: () -> Unit,
    coverageCount: Int = 1,
    onCoverageClick: () -> Unit = {},
    interactionViewModel: ArticleInteractionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val interactionStates by interactionViewModel.interactionStates.collectAsState()
    val interactionState = interactionStates[article.url]

    LaunchedEffect(article.url) {
        interactionViewModel.loadInteractions(article.url)
    }

    val upvoteCount  = interactionState?.upvotes ?: 0
    val userVote     = interactionState?.userVote
    val isUpvoted    = userVote == true
    val isDownvoted  = userVote == false
    val isBookmarked = interactionState?.isBookmarked == true

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )
    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bookmark_scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onArticleClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Featured Image ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                SubcomposeAsyncImage(
                    model = article.urlToImage,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { ShimmerBox() },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = getSourceBadgeColor(article.source.name).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = article.source.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = getSourceBadgeColor(article.source.name),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (coverageCount > 1) {
                        AssistChip(
                            onClick = onCoverageClick,
                            label = { Text("$coverageCount sources") },
                            modifier = Modifier.height(30.dp)
                        )
                    } else if (article.isOffline) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Offline") },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val readTimeMin = com.abpvt.newsapp.utils.ReadingTimeCalculator.calculateMinutes(
                            article.title, article.description, article.content
                        )
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MetadataText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${readTimeMin}m read",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetadataText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatPublishedTime(article.publishedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MetadataText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val description = article.description
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                var showShareDialog by remember { mutableStateOf(false) }

                if (showShareDialog) {
                    com.abpvt.newsapp.ui.news.components.ShareStoryDialog(
                        article = article,
                        onDismiss = { showShareDialog = false }
                    )
                }

                // ── Action Bar ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Vote pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (MaterialTheme.colorScheme.background == Color(0xFFF4F6FB))
                            ButtonBackground else Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            IconButton(
                                onClick = { interactionViewModel.vote(article.url, true) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Upvote",
                                    tint = if (isUpvoted) UpvoteActive else IconInactive,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = upvoteCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isUpvoted) UpvoteActive else IconInactive
                            )
                            VerticalDivider(
                                color = DividerColor,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(18.dp)
                            )
                            IconButton(
                                onClick = { interactionViewModel.vote(article.url, false) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Downvote",
                                    tint = if (isDownvoted) DownvoteActive else IconInactive,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Comment pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (MaterialTheme.colorScheme.background == Color(0xFFF4F6FB))
                            ButtonBackground else Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { onCommentClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bookmark
                    IconButton(
                        onClick = { interactionViewModel.toggleBookmark(article) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) BookmarkActive else IconInactive,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(bookmarkScale)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = IconInactive,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerBox() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue  = 1000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
                    start  = Offset(shimmerX, 0f),
                    end    = Offset(shimmerX + 400f, 400f)
                )
            )
    )
}

private fun getSourceBadgeColor(sourceName: String): Color {
    val colors = listOf(SourceBadge1, SourceBadge2, SourceBadge3, SourceBadge4, SourceBadge5)
    val index  = (sourceName.hashCode() % colors.size).let { if (it < 0) it + colors.size else it }
    return colors[index]
}

private fun formatPublishedTime(publishedAt: String): String {
    return try {
        val fmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = fmt.parse(publishedAt)
        val diff = Date().time - (date?.time ?: 0)
        val mins = diff / 60000
        val hrs  = mins / 60
        val days = hrs  / 24
        when {
            days > 0  -> "${days}d ago"
            hrs  > 0  -> "${hrs}h ago"
            mins > 0  -> "${mins}m ago"
            else      -> "Just now"
        }
    } catch (e: Exception) {
        publishedAt.takeIf { it.isNotBlank() } ?: ""
    }
}
