package com.abpvt.newsapp.ui.comments


import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abpvt.newsapp.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onVote: (isUpvote: Boolean) -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Compute upvote and downvote counts from the voters map
    val upVotes = comment.voters.values.count { it }
    val downVotes = comment.voters.values.count { !it }
    
    // Check if current user has voted
    val userVote = currentUserId?.let { comment.voters[it] }
    val hasUpvoted = userVote == true
    val hasDownvoted = userVote == false

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (comment.parentCommentId != null) 24.dp else 8.dp,  // Indent replies
                end = 8.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        // Display username instead of userId
        Text(
            text = "User: ${comment.username}",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.content,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Format timestamp into readable date/time
        val dateText = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(comment.timestamp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Vote buttons with counts and Reply button
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Upvote button - highlighted if user has upvoted
            Button(
                onClick = { onVote(true) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (hasUpvoted) 
                        MaterialTheme.colors.primary 
                    else 
                        MaterialTheme.colors.surface
                )
            ) {
                Text(
                    text = "↑ $upVotes",
                    color = if (hasUpvoted) Color.White else MaterialTheme.colors.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            // Downvote button - highlighted if user has downvoted
            Button(
                onClick = { onVote(false) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (hasDownvoted) 
                        MaterialTheme.colors.secondary 
                    else 
                        MaterialTheme.colors.surface
                )
            ) {
                Text(
                    text = "↓ $downVotes",
                    color = if (hasDownvoted) Color.White else MaterialTheme.colors.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Reply button
            TextButton(onClick = onReply) {
                Text("Reply")
            }
        }
    }
}
