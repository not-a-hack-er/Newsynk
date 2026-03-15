package com.abpvt.newsapp.ui.news

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Local color definitions
private val NewsCardBg    = Color(0xFFFFFFFF)
private val CardShadow    = Color(0x14000000)

@Composable
fun NewsItem(
    article: Article,
    onArticleClick: () -> Unit,
    onCommentClick: () -> Unit,
    interactionViewModel: ArticleInteractionViewModel = viewModel()
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

    // ── Press feedback: scale down card when pressed ──────────────────────────
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    // ── Bookmark pulse ────────────────────────────────────────────────────────
    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bookmark_scale"
    )

    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = NewsCardBg,
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
                            Text("📰", fontSize = 48.sp)
                        }
                    }
                )

                // Gradient overlay at the bottom of image for text contrast
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

            // ── Content Section ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Source badge + time row
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
                            style = MaterialTheme.typography.caption.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = getSourceBadgeColor(article.source.name),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = formatPublishedTime(article.publishedAt),
                        style = MaterialTheme.typography.caption.copy(fontSize = 11.sp),
                        color = MetadataText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 24.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TitleText
                )

                if (!article.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = DescriptionText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = DividerColor.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(10.dp))

                // ── Action Bar ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Vote pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ButtonBackground
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
                            Divider(
                                color = DividerColor,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .height(18.dp)
                                    .width(1.dp)
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
                        color = ButtonBackground,
                        modifier = Modifier.clickable { onCommentClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("💬", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bookmark
                    IconButton(
                        onClick = { interactionViewModel.toggleBookmark(article) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) BookmarkActive else IconInactive,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(bookmarkScale)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type   = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                        },
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

// ── Shimmer Loading Placeholder ───────────────────────────────────────────────
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
