package com.abpvt.newsapp.ui.comments



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abpvt.newsapp.data.model.Comment
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CommentScreen(
    articleId: String,
    commentViewModel: CommentViewModel = viewModel()
) {
    // Load comments when this screen appears or when articleId changes
    LaunchedEffect(articleId) {
        commentViewModel.loadComments(articleId)
    }

    // Collect state from ViewModel
    val comments by commentViewModel.comments.collectAsState()
    val isLoading by commentViewModel.isLoading.collectAsState()
    val error by commentViewModel.error.collectAsState()
    val replyingTo by commentViewModel.replyingTo.collectAsState()

    // Get current user ID for vote state
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Local state for the new comment text input
    var newComment by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        // Show error message if any
        if (error != null) {
            Text(
                text = error ?: "",
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Show loading indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // List of comments
        LazyColumn(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    currentUserId = currentUserId,
                    onVote = { isUpvote -> commentViewModel.vote(comment.id, isUpvote) },
                    onReply = { commentViewModel.setReplyingTo(comment) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show reply indicator if replying to a comment
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Replying to @${replyingTo?.username}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
                TextButton(onClick = { commentViewModel.clearReply() }) {
                    Text("Cancel")
                }
            }
        }

        // Text field for new comment
        OutlinedTextField(
            value = newComment,
            onValueChange = { newComment = it },
            label = { Text(if (replyingTo != null) "Add a reply" else "Add a comment") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Submit button
        Button(
            onClick = {
                commentViewModel.postComment(articleId, newComment)
                newComment = ""
            },
            enabled = newComment.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}
