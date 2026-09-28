package com.abpvt.newsapp.ui.comments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abpvt.newsapp.data.model.Comment
import com.abpvt.newsapp.ui.theme.DeepBlue
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    articleId: String,
    commentViewModel: CommentViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    LaunchedEffect(articleId) {
        commentViewModel.loadComments(articleId)
    }

    val comments by commentViewModel.comments.collectAsState()
    val isLoading by commentViewModel.isLoading.collectAsState()
    val error by commentViewModel.error.collectAsState()
    val replyingTo by commentViewModel.replyingTo.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var newComment by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Comments") },
                navigationIcon = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                } else {
                    {}
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Error banner
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Comments list
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        currentUserId = currentUserId,
                        onVote = { isUpvote -> commentViewModel.vote(comment.id, isUpvote) },
                        onReply = { commentViewModel.setReplyingTo(comment) },
                        onReport = { commentViewModel.report(comment) },
                        onBlock = { commentViewModel.block(comment) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reply banner
            if (replyingTo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Replying to @${replyingTo?.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { commentViewModel.clearReply() }) { Text("Cancel") }
                }
            }

            // Input field
            OutlinedTextField(
                value = newComment,
                onValueChange = { if (it.length <= 1000) newComment = it },
                label = { Text(if (replyingTo != null) "Add a reply" else "Add a comment") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${newComment.length}/1000") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

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
}
