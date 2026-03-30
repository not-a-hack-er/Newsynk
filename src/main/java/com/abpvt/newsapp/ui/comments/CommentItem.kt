package com.abpvt.newsapp.ui.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.newsapp.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccentBlue = Color(0xFF1565C0)
private val UpvoteGreen = Color(0xFF2E7D32)
private val DownvoteRed = Color(0xFFC62828)
private val ReplyColor = Color(0xFF546E7A)

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onVote: (isUpvote: Boolean) -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upVotes = comment.voters.values.count { it }
    val downVotes = comment.voters.values.count { !it }
    val userVote = currentUserId?.let { comment.voters[it] }
    val hasUpvoted = userVote == true
    val hasDownvoted = userVote == false
    val isReply = comment.parentCommentId != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 28.dp else 0.dp,
                end = 0.dp,
                top = 6.dp,
                bottom = 2.dp
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = if (isReply) 0.dp else 2.dp,
        backgroundColor = if (isReply) Color(0xFFF5F7FA) else MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Avatar + username row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(AccentBlue, Color(0xFF42A5F5))),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (comment.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentBlue
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(comment.timestamp)),
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment body
            Text(
                text = comment.content,
                style = MaterialTheme.typography.body2.copy(lineHeight = 20.sp),
                color = MaterialTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Upvote
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (hasUpvoted) UpvoteGreen.copy(alpha = 0.12f) else Color(0xFFF0F0F0)
                ) {
                    TextButton(
                        onClick = { onVote(true) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "▲ $upVotes",
                            fontSize = 12.sp,
                            fontWeight = if (hasUpvoted) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasUpvoted) UpvoteGreen else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Downvote
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (hasDownvoted) DownvoteRed.copy(alpha = 0.10f) else Color(0xFFF0F0F0)
                ) {
                    TextButton(
                        onClick = { onVote(false) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "▼ $downVotes",
                            fontSize = 12.sp,
                            fontWeight = if (hasDownvoted) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasDownvoted) DownvoteRed else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Reply
                TextButton(
                    onClick = onReply,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "↩ Reply",
                        fontSize = 12.sp,
                        color = ReplyColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
