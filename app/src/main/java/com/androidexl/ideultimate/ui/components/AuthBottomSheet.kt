package com.androidexl.ideultimate.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.CommunityStoreRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

/**
 * Bottom sheet displayed when the user is not logged in:
 * - Tab 0: تسجيل الدخول (Email + Password)
 * - Tab 1: تسجيل دخول جديد (Icon from device + Name + Email + Password + Confirm Password)
 * Connects directly to Firebase Auth & Realtime Database (`/users/<userId>`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    storeRepository: CommunityStoreRepository,
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 0 = Sign In (تسجيل الدخول), 1 = Sign Up (تسجيل دخول جديد)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var name by rememberSaveable { mutableStateOf("") }
    var avatarPath by rememberSaveable { mutableStateOf<String?>(null) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val errNameEmpty = stringResource(R.string.error_name_empty)
    val errEmailInvalid = stringResource(R.string.auth_error_email_invalid)
    val errPasswordShort = stringResource(R.string.auth_error_password_short)
    val errPasswordMismatch = stringResource(R.string.auth_error_password_mismatch)
    val errEmailTaken = stringResource(R.string.auth_error_email_taken)
    val errInvalidCredentials = stringResource(R.string.auth_error_invalid_credentials)

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val saved = savePickedProjectIcon(context, uri)
            if (saved != null) {
                avatarPath = saved
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = Color(0xFF0B1028),
        contentColor = Ink,
        scrimColor = Color(0xD903060F),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (selectedTab == 0) {
                    stringResource(R.string.auth_title_login)
                } else {
                    stringResource(R.string.auth_title_register)
                },
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )

            Spacer(Modifier.height(14.dp))

            // Mode Switch Tabs: تسجيل الدخول | تسجيل دخول جديد
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x16070B1E))
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AuthModeTabButton(
                    label = stringResource(R.string.auth_tab_login),
                    selected = selectedTab == 0,
                    accent = Cyan,
                    onClick = {
                        if (!isSubmitting) {
                            selectedTab = 0
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                AuthModeTabButton(
                    label = stringResource(R.string.auth_tab_register),
                    selected = selectedTab == 1,
                    accent = VioletSoft,
                    onClick = {
                        if (!isSubmitting) {
                            selectedTab = 1
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            if (selectedTab == 1) {
                // Avatar Picker + Name for New Registration (تسجيل دخول جديد)
                Box(contentAlignment = Alignment.BottomEnd) {
                    UserAvatarView(
                        avatarPath = avatarPath,
                        size = 82.dp,
                        onClick = {
                            pickAvatarLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Violet, Cyan)))
                            .border(1.5.dp, Color(0xFF06091A), CircleShape)
                            .clickable {
                                pickAvatarLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Ink,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.auth_pick_avatar_hint),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Cyan,
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.auth_field_name)) },
                    singleLine = true,
                    colors = authFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(10.dp))
            }

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.auth_field_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    color = Ink,
                ),
                colors = authFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.auth_field_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = authFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            if (selectedTab == 1) {
                Spacer(Modifier.height(10.dp))

                // Confirm Password field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text(stringResource(R.string.auth_field_confirm_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = authFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Fuchsia.copy(alpha = 0.16f))
                        .border(1.dp, Fuchsia.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Fuchsia,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            GradientButton(
                text = when {
                    isSubmitting -> "Firebase..."
                    selectedTab == 0 -> stringResource(R.string.auth_btn_login)
                    else -> stringResource(R.string.auth_btn_register)
                },
                onClick = {
                    if (isSubmitting) return@GradientButton
                    val cleanEmail = email.trim()
                    if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                        errorMessage = errEmailInvalid
                        return@GradientButton
                    }
                    if (password.length < 6) {
                        errorMessage = errPasswordShort
                        return@GradientButton
                    }

                    if (selectedTab == 0) {
                        isSubmitting = true
                        storeRepository.signInWithEmailPasswordAsync(
                            email = cleanEmail,
                            password = password,
                        ) { success, _ ->
                            isSubmitting = false
                            if (success) {
                                onAuthenticated()
                            } else {
                                errorMessage = errInvalidCredentials
                            }
                        }
                    } else {
                        val cleanName = name.trim()
                        if (cleanName.isEmpty()) {
                            errorMessage = errNameEmpty
                            return@GradientButton
                        }
                        if (password != confirmPassword) {
                            errorMessage = errPasswordMismatch
                            return@GradientButton
                        }
                        isSubmitting = true
                        storeRepository.registerNewAccountAsync(
                            name = cleanName,
                            email = cleanEmail,
                            password = password,
                            avatarPath = avatarPath,
                        ) { success, errCode ->
                            isSubmitting = false
                            if (success) {
                                onAuthenticated()
                            } else if (errCode == "EMAIL_TAKEN") {
                                errorMessage = errEmailTaken
                            } else {
                                errorMessage = errCode ?: errEmailTaken
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AuthModeTabButton(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(200),
        label = "auth-tab-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else Color.Transparent,
        animationSpec = tween(200),
        label = "auth-tab-border",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = if (selected) accent else Muted,
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Cyan,
    unfocusedBorderColor = Color(0x33FFFFFF),
    focusedLabelColor = Cyan,
    unfocusedLabelColor = Muted,
    cursorColor = Cyan,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedContainerColor = Color(0x16070B1E),
    unfocusedContainerColor = Color(0x16070B1E),
)
