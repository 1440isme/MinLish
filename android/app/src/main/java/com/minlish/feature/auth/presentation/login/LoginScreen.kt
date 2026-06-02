package com.minlish.feature.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.minlish.BuildConfig
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.loginWithGoogle(idToken)
                Toast.makeText(context, context.getString(R.string.auth_google_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.auth_google_null_token), Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, context.getString(R.string.auth_google_failed, e.statusCode), Toast.LENGTH_LONG).show()
        }
    }

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
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.login_email_placeholder),
                                style = MinLishTypography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
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
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.login_password_placeholder),
                                style = MinLishTypography.bodyMedium,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
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
                            Toast.makeText(context, context.getString(R.string.auth_forgot_password_mock), Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp)
                )
            }

            // Log in Button with loading state
            Button(
                onClick = {
                    if (email.trim().isEmpty() || password.trim().isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.auth_login_fields_required), Toast.LENGTH_SHORT).show()
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

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.08f))
                Text(
                    text = stringResource(id = R.string.auth_or_divider),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.08f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google sign-in button
            OutlinedButton(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = ButtonCapsuleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
            ) {
                GoogleLogo(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.auth_google_button),
                    style = MinLishTypography.labelLarge.copy(fontSize = 16.sp),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

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

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = w / 2

        // Red sector (top):
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Yellow sector (left):
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Green sector (bottom):
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true
        )
        // Blue sector (right):
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true
        )

        // Draw inner white circle to make it a donut G shape
        drawCircle(
            color = Color.White,
            radius = r * 0.45f
        )
    }
}
