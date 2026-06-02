package com.minlish.feature.profile.presentation

import android.widget.Toast
import com.minlish.core.utils.showToast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.minlish.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.minlish.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileSettingsViewModel , onNavigateToSettings: () -> Unit ) //lệnh chuyển màn hình từ NavHost truyền vào
{
    val name by viewModel.fullName.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val email by viewModel.email.collectAsState()
    val goal by viewModel.learningGoal.collectAsState()
    val currentGoalWords by viewModel.dailyNewWordsGoal.collectAsState()
    val targetLvl by viewModel.targetLevel.collectAsState()
    val targetLevelId by viewModel.targetLevelId.collectAsState()
    val learningLevels by viewModel.learningLevels.collectAsState()

    val context = LocalContext.current
    var showGoalDialog by remember { mutableStateOf(false) }
    var showGoalTypeDialog by remember { mutableStateOf(false) }
    var showTargetLevelDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var tempAvatar by remember { mutableStateOf("") }

    val accentTeal = Color(0xFF0D9488)
    val bgLight = Color(0xFFFFF9F2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgLight)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1C1C1A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Start
        )

        // Avatar rendering with Edit option
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = stringResource(R.string.profile_title),
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable {
                        tempName = name
                        tempAvatar = avatarUrl
                        showEditNameDialog = true
                    },
                contentScale = ContentScale.Crop
            )
        } else {
            // Initials avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(accentTeal)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable {
                        tempName = name
                        tempAvatar = avatarUrl
                        showEditNameDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                val initials = name.split(" ")
                    .mapNotNull { it.firstOrNull() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                
                Text(
                    text = initials.ifEmpty { "U" },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Full name clickable Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    tempName = name
                    tempAvatar = avatarUrl
                    showEditNameDialog = true
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1A)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.profile_edit_name_title),
                tint = accentTeal,
                modifier = Modifier.size(16.dp)
            )
        }

        // Email address
        Text(
            text = email.ifEmpty { "—" },
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Profile Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Item 1: Learning Goal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGoalTypeDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = accentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_learning_goal),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1A)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = goal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accentTeal
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                // Item 2: Target Score
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTargetLevelDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = accentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_target_level),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1A)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = targetLvl,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accentTeal
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                // Item 3: Daily Target Words
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGoalDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = accentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_daily_words),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1A)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.profile_words_count, currentGoalWords),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accentTeal
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }

                //Item 4: App setting
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSettings() } // Kích hoạt bắn lệnh chuyển đổi vùng màn hình
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = accentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_app_settings),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1A)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                }
                //----------
            }
        }

        Spacer(modifier = Modifier.height(20.dp)) // Add distance/spacing between settings container and logout button

        // Google Sign-In client for sign out
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build()
        }
        val googleSignInClient = remember {
            GoogleSignIn.getClient(context, googleSignInOptions)
        }

        // Log out button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    googleSignInClient.signOut().addOnCompleteListener {
                        viewModel.logout()
                        showToast(context, context.getString(R.string.profile_logout_success), Toast.LENGTH_SHORT)
                    }
                },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.profile_sign_out), tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.profile_sign_out),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Red
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f))
            }
        }

        // Daily goal selector Dialog
        if (showGoalDialog) {
            AlertDialog(
                onDismissRequest = { showGoalDialog = false },
                title = { Text(stringResource(R.string.profile_set_daily_goal), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_select_words_desc), fontSize = 14.sp, color = Color.Gray)
                        
                        var selectedVal by remember { mutableStateOf(currentGoalWords) }
                        val goalOptions = listOf(5, 10, 20, 30)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goalOptions.forEach { opt ->
                                val isSelected = opt == selectedVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentTeal else Color.LightGray.copy(alpha = 0.2f))
                                        .clickable { selectedVal = opt }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$opt",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showGoalDialog = false }) {
                                Text(stringResource(R.string.common_cancel), color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateDailyGoal(selectedVal)
                                    showGoalDialog = false
                                    showToast(context, context.getString(R.string.profile_goal_updated), Toast.LENGTH_SHORT)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                            ) {
                                Text(stringResource(R.string.common_save), color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {},
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }

        // Daily goal type selector Dialog
        if (showGoalTypeDialog) {
            AlertDialog(
                onDismissRequest = { showGoalTypeDialog = false },
                title = { Text(stringResource(R.string.profile_learning_goal), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_select_goal_desc), fontSize = 14.sp, color = Color.Gray)
                        
                        var selectedVal by remember { mutableStateOf(goal) }
                        val goalOptions = listOf("TOEIC", "IELTS")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goalOptions.forEach { opt ->
                                val isSelected = opt == selectedVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentTeal else Color.LightGray.copy(alpha = 0.2f))
                                        .clickable { selectedVal = opt }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showGoalTypeDialog = false }) {
                                Text(stringResource(R.string.common_cancel), color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateLearningGoal(selectedVal)
                                    showGoalTypeDialog = false
                                    showToast(context, context.getString(R.string.profile_goal_updated_success), Toast.LENGTH_SHORT)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                            ) {
                                Text(stringResource(R.string.common_save), color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {},
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }

        // Target Level Selector Dialog
        if (showTargetLevelDialog) {
            AlertDialog(
                onDismissRequest = { showTargetLevelDialog = false },
                title = { Text(stringResource(R.string.profile_target_level), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_select_level_desc), fontSize = 14.sp, color = Color.Gray)
                        
                        val filteredLevels = learningLevels.filter { level ->
                            level.code.startsWith(goal ?: "TOEIC", ignoreCase = true)
                        }
                        val levelOptions = filteredLevels.map { it.id to it.name }.ifEmpty {
                            if (goal == "TOEIC") {
                                listOf(
                                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1" to "TOEIC 450+",
                                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2" to "TOEIC 600+",
                                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3" to "TOEIC 750+",
                                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4" to "TOEIC 900+"
                                )
                            } else {
                                listOf(
                                    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1" to "IELTS 4.0+",
                                    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2" to "IELTS 5.5+",
                                    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3" to "IELTS 6.5+",
                                    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4" to "IELTS 7.0+"
                                )
                            }
                        }

                        var selectedId by remember { mutableStateOf(targetLevelId) }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            levelOptions.forEach { (id, label) ->
                                val isSelected = id == selectedId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentTeal else Color.LightGray.copy(alpha = 0.2f))
                                        .clickable { selectedId = id }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTargetLevelDialog = false }) {
                                Text(stringResource(R.string.common_cancel), color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (selectedId.isNotEmpty()) {
                                        viewModel.updateTargetLevel(selectedId)
                                    }
                                    showTargetLevelDialog = false
                                    showToast(context, context.getString(R.string.profile_target_level_updated), Toast.LENGTH_SHORT)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                            ) {
                                Text(stringResource(R.string.common_save), color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {},
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }

        // Edit Profile Dialog
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text(stringResource(R.string.profile_edit_title), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Full name input
                        Column {
                            Text(
                                text = stringResource(R.string.profile_change_name),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Avatar URL input
                        Column {
                            Text(
                                text = stringResource(R.string.profile_change_avatar),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = tempAvatar,
                                onValueChange = { tempAvatar = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("https://example.com/avatar.png", color = Color.LightGray) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEditNameDialog = false }) {
                                Text(stringResource(R.string.common_cancel), color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (tempName.trim().length < 2) {
                                        showToast(context, context.getString(R.string.profile_name_min_length), Toast.LENGTH_SHORT)
                                    } else {
                                        viewModel.updateProfile(tempName.trim(), tempAvatar.trim())
                                        showEditNameDialog = false
                                        showToast(context, context.getString(R.string.profile_updated_success), Toast.LENGTH_SHORT)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                            ) {
                                Text(stringResource(R.string.common_save), color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {},
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }
    }
}
