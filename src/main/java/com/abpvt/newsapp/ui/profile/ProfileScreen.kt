package com.abpvt.newsapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.auth.AuthState
import com.abpvt.newsapp.auth.AuthViewModel
import com.abpvt.newsapp.navigation.Screen

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val profileUpdateSuccess by authViewModel.profileUpdateSuccess.collectAsState()

    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUserEmail = firebaseUser?.email ?: "Unknown Email"
    
    // Local state for editing name
    var currentUserName by remember(firebaseUser?.displayName) { 
        mutableStateOf(firebaseUser?.displayName ?: "No Name Set") 
    }
    var isEditingName by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }
    
    // Handle successful profile update
    LaunchedEffect(profileUpdateSuccess) {
        if (profileUpdateSuccess) {
            isEditingName = false
            currentUserName = editNameInput
            authViewModel.resetProfileUpdateState()
        }
    }

    // If user logs out successfully, navigate back to Login
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate(Screen.Login.route) {
                // Clear the back stack to prevent going back to profile
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 4.dp
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // User Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Account Details",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Name:",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (isEditingName) {
                            OutlinedTextField(
                                value = editNameInput,
                                onValueChange = { editNameInput = it },
                                modifier = Modifier.weight(1f).padding(start = 16.dp),
                                singleLine = true,
                                trailingIcon = {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        Row {
                                            TextButton(onClick = { isEditingName = false }) {
                                                Text("Cancel")
                                            }
                                            TextButton(
                                                onClick = { authViewModel.updateUsername(editNameInput) },
                                                enabled = editNameInput.isNotBlank()
                                            ) {
                                                Text("Save")
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUserName,
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                                )
                                IconButton(
                                    onClick = {
                                        editNameInput = if (currentUserName == "No Name Set") "" else currentUserName
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Name",
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Email:",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentUserEmail,
                            style = MaterialTheme.typography.body1,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 6.dp) // align with the edit button
                        )
                    }
                }
            }

            // Bookmarks Button
            Button(
                onClick = { navController.navigate(Screen.Bookmarks.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondaryVariant,
                    contentColor = Color.White
                )
            ) {
                 Icon(Icons.Default.Star, contentDescription = "Bookmarks", modifier = Modifier.padding(end = 8.dp))
                 Text(
                     text = "View Saved Articles",
                     fontSize = 16.sp,
                     fontWeight = FontWeight.Bold
                 )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = { authViewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
