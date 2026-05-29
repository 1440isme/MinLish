package com.minlish.feature.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.R
import com.minlish.core.component.BookIllustration
import com.minlish.core.component.RobotIllustration
import com.minlish.core.component.StarIllustration
import com.minlish.feature.auth.presentation.AuthUiState
import com.minlish.feature.auth.presentation.AuthViewModel
import com.minlish.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            val errorMsg = (uiState as AuthUiState.Error).message
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            authViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWarm)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToLogin) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Account",
                    style = MinLishTypography.displayMedium.copy(fontSize = 22.sp),
                    color = TextPrimary
                )
            }

            // Main Title
            Text(
                text = "Join MinLish",
                style = MinLishTypography.displayLarge.copy(
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center
                ),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Form container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                Column {
                    Text(
                        text = "Full Name",
                        style = MinLishTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = ButtonCapsuleShape
                    )
                }

                // Email
                Column {
                    Text(
                        text = stringResource(id = R.string.login_email_label),
                        style = MinLishTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = ButtonCapsuleShape
                    )
                }

                // Password
                Column {
                    Text(
                        text = stringResource(id = R.string.login_password_label),
                        style = MinLishTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = ButtonCapsuleShape
                    )
                }

                // Confirm Password
                Column {
                    Text(
                        text = "Confirm Password",
                        style = MinLishTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = ButtonCapsuleShape
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Register Button with client validation & loading indicator
            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    val trimmedEmail = email.trim()
                    val trimmedPass = password
                    val passRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")

                    if (trimmedName.length < 2) {
                        Toast.makeText(context, "Full name must be at least 2 characters", Toast.LENGTH_SHORT).show()
                    } else if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
                        Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show()
                    } else if (trimmedPass.length < 8) {
                        Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                    } else if (!trimmedPass.matches(passRegex)) {
                        Toast.makeText(context, "Password must contain at least 1 uppercase letter, 1 lowercase letter, and 1 digit", Toast.LENGTH_LONG).show()
                    } else if (trimmedPass != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        authViewModel.register(trimmedEmail, trimmedPass, trimmedName)
                    }
                },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = ButtonCapsuleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextPrimary,
                    contentColor = SurfaceWhite
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign up",
                        style = MinLishTypography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Mascot Illustrations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RobotIllustration(modifier = Modifier.padding(horizontal = 8.dp))
                BookIllustration(modifier = Modifier.padding(horizontal = 8.dp))
                StarIllustration(modifier = Modifier.padding(horizontal = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back link
            Row(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MinLishTypography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Log in",
                    style = MinLishTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }
    }
}
