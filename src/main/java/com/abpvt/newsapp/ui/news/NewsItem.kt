package com.abpvt.newsapp.ui.news


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abpvt.newsapp.data.model.Article
import java.text.SimpleDateFormat
import java.util.*

// Local color definitions to resolve build issues
private val NewsCardBackground = Color(0xFFFAFAFA)
private val GradientStart = Color(0xFF1E88E5)
private val GradientMid = Color(0xFF42A5F5)
private val GradientEnd = Color(0xFF00BCD4)
private val PlaceholderDark = Color(0xFF37474F)
private val TitleText = Color(0xFF1A1A1A)
private val SourceBadge1 = Color(0xFF2196F3)
private val SourceBadge2 = Color(0xFF9C27B0)
private val SourceBadge3 = Color(0xFF009688)
private val SourceBadge4 = Color(0xFFFF6B35)
private val SourceBadge5 = Color(0xFFE91E63)
private val DescriptionText = Color(0xFF616161)
private val MetadataText = Color(0xFF9E9E9E)
private val ButtonBackground = Color(0xFFEEEEEE)
private val UpvoteActive = Color(0xFF2196F3)
private val IconInactive = Color(0xFF757575)
private val DividerColor = Color(0xFFBDBDBD)
private val DownvoteActive = Color(0xFFF44336)

/**
 * Modern card-based news item with vibrant colors, gradient backgrounds, dynamic source badges,
 * and interactive upvote/downvote/comment buttons.
 */
@Composable
fun NewsItem(
    article: Article,
    onArticleClick: () -> Unit,
    onCommentClick: () -> Unit,
    interactionViewModel: ArticleInteractionViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Get interaction state
    val interactionStates by interactionViewModel.interactionStates.collectAsState()
    val interactionState = interactionStates[article.url]

    // Fetch on initial load
    LaunchedEffect(article.url) {
        interactionViewModel.loadInteractions(article.url)
    }

    val upvoteCount = interactionState?.upvotes ?: 0
    val userVote = interactionState?.userVote
    val isUpvoted = userVote == true
    val isDownvoted = userVote == false
    val isBookmarked = interactionState?.isBookmarked == true

    // State for comment interactions
    var commentCount by remember { mutableStateOf(0) }

    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = NewsCardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArticleClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Featured Image Section
            SubcomposeAsyncImage(
                model = article.urlToImage,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                loading = {
                    // Loading placeholder with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GradientStart.copy(alpha = 0.3f),
                                        GradientMid.copy(alpha = 0.3f),
                                        GradientEnd.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                },
                error = {
                    // Error placeholder with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PlaceholderDark,
                                        PlaceholderDark.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
            )

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TitleText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Source Badge with dynamic color
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getSourceBadgeColor(article.source.name),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = article.source.name,
                        style = MaterialTheme.typography.caption.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                if (!article.description.isNullOrBlank()) {
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = DescriptionText
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Published Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatPublishedTime(article.publishedAt),
                        style = MaterialTheme.typography.caption.copy(
                            fontSize = 12.sp
                        ),
                        color = MetadataText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote/Downvote Section
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ButtonBackground,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            // Upvote Button
                            IconButton(
                                onClick = {
                                    interactionViewModel.vote(article.url, true)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Upvote",
                                    tint = if (isUpvoted) UpvoteActive else IconInactive,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Upvote Count
                            Text(
                                text = upvoteCount.toString(),
                                style = MaterialTheme.typography.body2.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = if (isUpvoted) UpvoteActive else IconInactive
                            )

                            // Divider
                            Divider(
                                color = DividerColor,
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .padding(horizontal = 4.dp)
                            )

                            // Downvote Button
                            IconButton(
                                onClick = {
                                    interactionViewModel.vote(article.url, false)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Downvote",
                                    tint = if (isDownvoted) DownvoteActive else IconInactive,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Comment Section
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = ButtonBackground,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clickable { onCommentClick() }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "💬",
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = commentCount.toString(),
                                style = MaterialTheme.typography.body2.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = IconInactive
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bookmark Button
                    IconButton(
                        onClick = { interactionViewModel.toggleBookmark(article) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colors.primary else IconInactive,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = IconInactive,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get dynamic badge color based on source name
 */
private fun getSourceBadgeColor(sourceName: String): Color {
    val colors = listOf(
        SourceBadge1,
        SourceBadge2,
        SourceBadge3,
        SourceBadge4,
        SourceBadge5
    )
    
    // Use hash of source name to consistently assign colors
    val hash = sourceName.hashCode()
    val index = (hash % colors.size).let { if (it < 0) it + colors.size else it }
    
    return colors[index]
}

/**
 * Format the published time to a more readable format
 */
private fun formatPublishedTime(publishedAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(publishedAt)
        
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        when {
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    } catch (e: Exception) {
        publishedAt.takeIf { it.isNotBlank() } ?: ""
    }
}
