package com.minlish.feature.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
fun LoginScreen(
    authViewModel: AuthViewModel,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("tester@example.com") }
    var password by remember { mutableStateOf("Password123") } // Fits validation schema
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    // Handle Auth side effects (like showing an error Toast)
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
                .padding(top = 40.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Top Title
            Text(
                text = stringResource(id = R.string.login_welcome_back),
                style = MinLishTypography.displayLarge.copy(
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center
                ),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Form container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email field
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_email_input"),
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

                // Password field (Correctly using visual transformation)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_password_input"),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(id = R.string.login_toggle_password_visibility),
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

            // Forgot password label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(id = R.string.login_forgot_password),
                    style = MinLishTypography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .clickable {
                            Toast.makeText(context, "Password recovery is mocked.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp)
                )
            }

            // Log in Button with loading state
            Button(
                onClick = {
                    if (email.trim().isEmpty() || password.trim().isEmpty()) {
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    } else {
                        authViewModel.login(email.trim(), password)
                    }
                },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_submit_button"),
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
                        text = stringResource(id = R.string.login_button_text),
                        style = MinLishTypography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            // Toggle to Registration Screen
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.login_new_to_minlish),
                    style = MinLishTypography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = " " + stringResource(id = R.string.login_sign_up),
                    style = MinLishTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}
