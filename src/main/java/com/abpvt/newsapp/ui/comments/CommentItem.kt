package com.abpvt.newsapp.ui.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.newsapp.data.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccentBlue  = Color(0xFF1565C0)
private val UpvoteGreen = Color(0xFF2E7D32)
private val DownvoteRed = Color(0xFFC62828)
private val ReplyColor  = Color(0xFF546E7A)

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onVote: (isUpvote: Boolean) -> Unit,
    onReply: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upVotes    = comment.voters.values.count { it }
    val downVotes  = comment.voters.values.count { !it }
    val userVote   = currentUserId?.let { comment.voters[it] }
    val hasUpvoted = userVote == true
    val hasDownvoted = userVote == false
    val isReply    = comment.parentCommentId != null
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 28.dp else 0.dp, top = 6.dp, bottom = 2.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isReply) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReply) Color(0xFFF5F7FA) else MaterialTheme.colorScheme.surface
        )
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
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentBlue
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(comment.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.weight(1f))
                if (comment.userId != currentUserId) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Comment options")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Report") },
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                onClick = { showMenu = false; onReport() }
                            )
                            DropdownMenuItem(
                                text = { Text("Block user") },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                                onClick = { showMenu = false; onBlock() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment body
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface
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
                    Text("↩ Reply", fontSize = 12.sp, color = ReplyColor, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
