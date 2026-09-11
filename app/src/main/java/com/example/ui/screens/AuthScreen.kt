package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthMode
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

/**
 * Accessible, Material Design 3 Authentication Screen for KAARIGAR.
 * Supports Email/Password Login, Registration, 1-Tap Guest Access, Password Reset,
 * and Persistent User Session management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArtisanBackground)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        AuthHeader(
            currentUser = currentUser,
            userSessionRole = userSession?.role
        )

        // Persistent Session Banner (if logged in)
        if (currentUser != null || userSession?.isLoggedIn == true) {
            PersistedSessionCard(
                user = currentUser,
                session = userSession,
                onSignOut = { viewModel.signOut() },
                onSendVerification = { viewModel.sendEmailVerification() }
            )
        }

        // Error & Success Feedback Banners
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.errorMessage?.let { errorMsg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("auth_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.successMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            uiState.successMessage?.let { successMsg ->
                Surface(
                    color = ForestSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("auth_success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = ForestSuccess
                        )
                        Text(
                            text = successMsg,
                            color = ForestSuccess,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Mode Navigation Tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TabRow(
                    selectedTabIndex = when (uiState.authMode) {
                        AuthMode.LOGIN -> 0
                        AuthMode.REGISTER -> 1
                        AuthMode.GUEST_ACCESS -> 2
                        AuthMode.FORGOT_PASSWORD -> 0
                    },
                    containerColor = ArtisanSurfaceVariant,
                    contentColor = TerracottaPrimary,
                    indicator = { tabPositions ->
                        val index = when (uiState.authMode) {
                            AuthMode.LOGIN -> 0
                            AuthMode.REGISTER -> 1
                            AuthMode.GUEST_ACCESS -> 2
                            AuthMode.FORGOT_PASSWORD -> 0
                        }
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = TerracottaPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.authMode == AuthMode.LOGIN,
                        onClick = { viewModel.setAuthMode(AuthMode.LOGIN) },
                        text = { Text("लॉगिन", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("auth_tab_login")
                    )
                    Tab(
                        selected = uiState.authMode == AuthMode.REGISTER,
                        onClick = { viewModel.setAuthMode(AuthMode.REGISTER) },
                        text = { Text("पंजीकरण", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("auth_tab_register")
                    )
                    Tab(
                        selected = uiState.authMode == AuthMode.GUEST_ACCESS,
                        onClick = { viewModel.setAuthMode(AuthMode.GUEST_ACCESS) },
                        text = { Text("1-टैप प्रवेश", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("auth_tab_guest")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                when (uiState.authMode) {
                    AuthMode.LOGIN -> {
                        LoginForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onForgotPassword = { viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) },
                            onSuccess = onAuthSuccess
                        )
                    }
                    AuthMode.REGISTER -> {
                        RegisterForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSuccess = onAuthSuccess
                        )
                    }
                    AuthMode.GUEST_ACCESS -> {
                        GuestAccessForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSuccess = onAuthSuccess
                        )
                    }
                    AuthMode.FORGOT_PASSWORD -> {
                        ForgotPasswordForm(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackToLogin = { viewModel.setAuthMode(AuthMode.LOGIN) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthHeader(
    currentUser: com.example.data.firebase.AuthUser?,
    userSessionRole: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = TerracottaPrimary.copy(alpha = 0.12f),
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security",
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = "कारीगर पहचान व क्लाउड सुरक्षा",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ArtisanTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Firebase Auth • सुरक्षित सत्र प्रबंधन • डेटा सुरक्षा",
            fontSize = 12.sp,
            color = ArtisanTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PersistedSessionCard(
    user: com.example.data.firebase.AuthUser?,
    session: com.example.data.preferences.UserSession?,
    onSignOut: () -> Unit,
    onSendVerification: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("persisted_session_info_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoSecondary.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IndigoSecondary.copy(alpha = 0.3f)))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = ForestSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "सक्रिय उपयोगकर्ता सत्र (Active Session)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoSecondary
                    )
                }

                Surface(
                    color = ForestSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "स्थायी (Saved)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "नाम (Name):", fontSize = 12.sp, color = ArtisanTextSecondary)
                Text(
                    text = user?.displayName ?: session?.displayName ?: "Artisan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArtisanTextPrimary
                )
            }

            if (user?.email != null || session?.email != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "ईमेल (Email):", fontSize = 12.sp, color = ArtisanTextSecondary)
                    Text(
                        text = user?.email ?: session?.email ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArtisanTextPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "भूमिका (Role):", fontSize = 12.sp, color = ArtisanTextSecondary)
                Text(
                    text = if ((session?.role ?: "ARTISAN") == "ARTISAN") "कारीगर (Artisan)" else "खरीदार (Buyer)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TerracottaPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.weight(1f).testTag("sign_out_session_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("लॉगआउट", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (user?.isAnonymous == false && user.email != null) {
                    TextButton(
                        onClick = onSendVerification,
                        modifier = Modifier.weight(1f).testTag("verify_email_button")
                    ) {
                        Text("ईमेल सत्यापन भेजें", fontSize = 11.sp, color = IndigoSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onForgotPassword: () -> Unit,
    onSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("ईमेल पता (Email Address)") },
            placeholder = { Text("artisan@kaushvani.in") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ArtisanTextSecondary)
            },
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth().testTag("auth_login_email_input")
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("पासवर्ड (Password)") },
            placeholder = { Text("कम से कम 6 अक्षर") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ArtisanTextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.loginWithEmail { onSuccess() }
            }),
            modifier = Modifier.fillMaxWidth().testTag("auth_login_password_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.rememberSession,
                    onCheckedChange = { viewModel.toggleRememberSession(it) }
                )
                Text("सत्र याद रखें", fontSize = 12.sp, color = ArtisanTextPrimary)
            }

            TextButton(onClick = onForgotPassword) {
                Text("पासवर्ड भूल गए?", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.loginWithEmail { onSuccess() }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_login_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("लॉगिन करें (Sign In)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RegisterForm(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Role Selector Chips
        Text(
            text = "आपकी भूमिका चुनें (Select Account Type):",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArtisanTextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = uiState.role == "ARTISAN",
                onClick = { viewModel.onRoleChange("ARTISAN") },
                label = { Text("शिल्पकार (Artisan)") },
                leadingIcon = {
                    if (uiState.role == "ARTISAN") {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                modifier = Modifier.weight(1f).testTag("register_role_artisan")
            )

            FilterChip(
                selected = uiState.role == "BUYER",
                onClick = { viewModel.onRoleChange("BUYER") },
                label = { Text("खरीदार (Buyer)") },
                leadingIcon = {
                    if (uiState.role == "BUYER") {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                modifier = Modifier.weight(1f).testTag("register_role_buyer")
            )
        }

        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = { viewModel.onDisplayNameChange(it) },
            label = { Text("पूरा नाम (Full Name)") },
            placeholder = { Text("उदा. राम प्रसाद प्रजापति") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ArtisanTextSecondary)
            },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = FocusDirection.Down.let { ImeAction.Next }),
            modifier = Modifier.fillMaxWidth().testTag("auth_register_name_input")
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("ईमेल पता (Email Address)") },
            placeholder = { Text("artisan@gmail.com") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ArtisanTextSecondary)
            },
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("auth_register_email_input")
        )

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { viewModel.onPhoneChange(it) },
            label = { Text("मोबाइल नंबर (Mobile Phone - ऐच्छिक)") },
            placeholder = { Text("+91 98765 43210") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = ArtisanTextSecondary)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.role == "ARTISAN") {
            OutlinedTextField(
                value = uiState.craftSpecialty,
                onValueChange = { viewModel.onCraftSpecialtyChange(it) },
                label = { Text("हस्तशिल्प विधा (Craft Specialty)") },
                placeholder = { Text("उदा. टेराकोटा, चिकनकारी, बंधेज") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Brush, contentDescription = null, tint = ArtisanTextSecondary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("पासवर्ड बनाएं (Create Password)") },
            placeholder = { Text("कम से कम 6 अक्षर") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ArtisanTextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().testTag("auth_register_password_input")
        )

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            label = { Text("पासवर्ड की पुष्टि करें (Confirm Password)") },
            placeholder = { Text("पासवर्ड पुनः दर्ज करें") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = ArtisanTextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                    Icon(
                        imageVector = if (uiState.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.confirmPasswordError != null,
            supportingText = uiState.confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.registerWithEmail { onSuccess() }
            }),
            modifier = Modifier.fillMaxWidth().testTag("auth_register_confirm_password_input")
        )

        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.registerWithEmail { onSuccess() }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_register_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("नया खाता बनाएं (Create Account)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GuestAccessForm(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    var guestName by remember { mutableStateOf(uiState.displayName) }
    var guestRole by remember { mutableStateOf("ARTISAN") }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = ArtisanSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = TerracottaPrimary)
                Text(
                    text = "बिना पासवर्ड के तुरंत 1-टैप गेस्ट के रूप में सुरक्षित रूप से KAUSHVANI प्लेटफॉर्म का उपयोग करें।",
                    fontSize = 12.sp,
                    color = ArtisanTextPrimary
                )
            }
        }

        OutlinedTextField(
            value = guestName,
            onValueChange = { guestName = it },
            label = { Text("आपका नाम (Display Name)") },
            placeholder = { Text("उदा. शिल्पकार बंधु") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = ArtisanTextSecondary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("guest_name_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = guestRole == "ARTISAN",
                onClick = { guestRole = "ARTISAN" },
                label = { Text("शिल्पकार (Artisan)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = guestRole == "BUYER",
                onClick = { guestRole = "BUYER" },
                label = { Text("खरीदार (Buyer)") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                viewModel.loginAnonymously(guestName, guestRole) { onSuccess() }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("guest_access_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("1-टैप गेस्ट प्रवेश करें", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ForgotPasswordForm(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "अपना पंजीकृत ईमेल दर्ज करें। हम आपको पासवर्ड रीसेट लिंक भेजेंगे।",
            fontSize = 13.sp,
            color = ArtisanTextSecondary
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("पंजीकृत ईमेल पता") },
            placeholder = { Text("artisan@kaushvani.in") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ArtisanTextSecondary)
            },
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("auth_forgot_email_input")
        )

        Button(
            onClick = { viewModel.sendPasswordResetEmail() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_send_reset_email_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("रीसेट लिंक भेजें", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("← वापस लॉगिन पर जाएं", fontSize = 13.sp, color = IndigoSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}
