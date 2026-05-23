package com.minlish.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.minlish.core.presentation.MinLishViewModel

@Composable
fun SettingsScreen(viewModel: MinLishViewModel) {
    val isMockOn by viewModel.isMockMode.collectAsState()
    val accentTeal = Color(0xFF0D9488)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFF4F9F8))
            .padding(16.dp)
    ) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Style Card of Settings options list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {

                // Option 1: Mock Server toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = accentTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Mock Offline Sandbox Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Maintains SQLite storage without a core backend server", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = isMockOn,
                        onCheckedChange = { viewModel.settingsRepository.setMockServiceOn(it) }
                    )
                }

                HorizontalDivider()

                // Option 2: Full profile reset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.resetAppData() }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Reset Onboarding Settings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Returns app to initialization flow inputs", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Information Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MinLish App Blueprint", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A clean English Vocabulary application using Jetpack Compose, designed around Spaced Repetition (SuperMemo-2 algorithm) matching complete professional and academic requirements.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
