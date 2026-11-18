package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.CaduceusUser
import com.example.data.model.UserRole
import com.example.data.repository.CaduceusRepository
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.PrimaryCrimson
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextLightGrey
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthAndRegistrationScreen(
    repository: CaduceusRepository,
    onAuthComplete: () -> Unit,
    onOpenAdminRoute: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.caduceus_logo),
                contentDescription = "CADUCEUS 2026 Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, AntiqueGold, CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CADUCEUS 2026",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "OFFICIAL FESTIVAL PORTAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AntiqueGold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(AntiqueGold, BorderDark)),
                    width = 1.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ENTER CADUCEUS 2026",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign in with your Google account to directly access the event countdown, Mohammed Irfan Star Night poster, digital pass, and live announcements.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLightGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            val clientId = try {
                                context.getString(R.string.default_web_client_id)
                            } catch (e: Exception) {
                                errorMessage = "Google Sign-In configuration missing"
                                isLoading = false
                                return@Button
                            }

                            val signInOption = GetSignInWithGoogleOption.Builder(clientId).build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(signInOption)
                                .build()

                            scope.launch {
                                try {
                                    val result = credentialManager.getCredential(context as Activity, request)
                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                        val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                                        val authUser = authResult.user
                                        val uName = authUser?.displayName ?: "Caduceus Attendee"
                                        val uEmail = authUser?.email?.takeIf { it.isNotBlank() }
                                            ?: authUser?.providerData?.firstOrNull { !it.email.isNullOrBlank() }?.email
                                            ?: ""
                                        val uUid = authUser?.uid ?: ""

                                        repository.registerUserProfile(
                                            uid = uUid,
                                            fullName = uName,
                                            email = uEmail,
                                            phone = "",
                                            collegeName = "CADUCEUS 2026",
                                            department = "Cultural Delegate",
                                            studentId = "",
                                            zodiacSign = "Scorpio"
                                        )
                                        onAuthComplete()
                                    }
                                } catch (e: GetCredentialCancellationException) {
                                    Log.w("CaduceusAuth", "Sign-in cancelled: ${e.message}")
                                } catch (e: Exception) {
                                    Log.e("CaduceusAuth", "Google sign-in error", e)
                                    errorMessage = e.localizedMessage ?: "Sign in failed"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Sign In",
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "SIGN IN WITH GOOGLE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = PrimaryCrimson,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dedicated /admin Route Entry Point
            OutlinedButton(
                onClick = onOpenAdminRoute,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("open_admin_route_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Portal",
                    tint = AntiqueGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADMIN DASHBOARD SIGN IN (/admin)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AntiqueGold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_creator_credit"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CREATED BY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "MD ATHAR ALAM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AntiqueGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AdminSignInScreen(
    repository: CaduceusRepository,
    onAdminAuthenticated: (CaduceusUser) -> Unit,
    onUnauthorizedUser: (CaduceusUser) -> Unit,
    onBackToApp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToApp,
                modifier = Modifier.testTag("admin_signin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = DarkBloodRed,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "ROUTE: /admin",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AntiqueGold,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.caduceus_logo),
            contentDescription = "CADUCEUS 2026 Admin Crest",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, AntiqueGold, CircleShape),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ADMIN COMMAND PORTAL",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                color = AntiqueGold,
                letterSpacing = 1.sp
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "RESTRICTED ORGANISER AUTHENTICATION",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(PrimaryCrimson, AntiqueGold)),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Admin Security",
                    tint = AntiqueGold,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ADMINISTRATOR GOOGLE SIGN-IN",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Authenticate with Google. Firestore RBAC security verifies whether your account holds Super Admin or Event Admin privileges before granting access.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextLightGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        val clientId = try {
                            context.getString(R.string.default_web_client_id)
                        } catch (_: Exception) {
                            errorMessage = "Google Sign-In configuration missing"
                            isLoading = false
                            return@Button
                        }

                        scope.launch {
                            try {
                                try {
                                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                } catch (_: Exception) {}

                                val signInOption = GetSignInWithGoogleOption.Builder(clientId).build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(signInOption)
                                    .build()

                                val result = credentialManager.getCredential(context as Activity, request)
                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                                    val authUser = authResult.user
                                    val uName = authUser?.displayName ?: "Caduceus Admin"
                                    val uEmail = authUser?.email?.takeIf { it.isNotBlank() }
                                        ?: authUser?.providerData?.firstOrNull { !it.email.isNullOrBlank() }?.email
                                        ?: ""
                                    val uUid = authUser?.uid ?: ""

                                    repository.registerUserProfile(
                                        uid = uUid,
                                        fullName = uName,
                                        email = uEmail,
                                        phone = "",
                                        collegeName = "CADUCEUS 2026",
                                        department = "Organising Committee",
                                        studentId = "",
                                        zodiacSign = "Scorpio"
                                    )

                                    // Verify role directly from Firestore backend
                                    val verifiedProfile = repository.getUserProfileOnce(uUid)
                                    if (verifiedProfile != null &&
                                        (verifiedProfile.role == UserRole.SUPER_ADMIN.name ||
                                            verifiedProfile.role == UserRole.EVENT_ADMIN.name)
                                    ) {
                                        onAdminAuthenticated(verifiedProfile)
                                    } else if (verifiedProfile != null) {
                                        onUnauthorizedUser(verifiedProfile)
                                    } else {
                                        errorMessage = "Unable to verify administrator profile in Firestore."
                                    }
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("CaduceusAdminAuth", "Cancelled: ${e.message}")
                            } catch (e: Exception) {
                                Log.e("CaduceusAdminAuth", "Admin sign-in error", e)
                                errorMessage = e.localizedMessage ?: "Admin authentication failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("admin_google_sign_in_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = TextWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Google Sign In",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIGN IN WITH ADMIN GOOGLE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = errorMessage!!,
                color = PrimaryCrimson,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdminUnauthorizedScreen(
    user: CaduceusUser,
    onSwitchAccount: () -> Unit,
    onReturnToApp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_unauthorized_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(PrimaryCrimson, BorderDark)),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Access Denied",
                    tint = PrimaryCrimson,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ADMIN ACCESS RESTRICTED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryCrimson,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The account ${user.email.ifBlank { user.fullName }} (Role: ${user.role}) does not have Super Admin or Event Admin authorization in Firestore.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextLightGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onSwitchAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_switch_account_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SIGN IN WITH ANOTHER GOOGLE ACCOUNT", style = MaterialTheme.typography.labelSmall.copy(color = TextWhite))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onReturnToApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_return_to_app_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("RETURN TO FESTIVAL APP", style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGold))
                }
            }
        }
    }
}
