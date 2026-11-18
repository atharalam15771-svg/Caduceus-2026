package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusEvent
import com.example.data.model.CaduceusUser
import com.example.data.model.FestSettings
import com.example.data.model.UserRole
import com.example.data.repository.CaduceusRepository
import com.example.data.util.CaduceusAudioManager
import com.example.data.util.CaduceusNotificationHelper
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.AdminSignInScreen
import com.example.ui.screens.AdminUnauthorizedScreen
import com.example.ui.screens.AuthAndRegistrationScreen
import com.example.ui.screens.DigitalPassScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HoroscopeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PendingApprovalScreen
import com.example.ui.screens.PersonalDetailsScreen
import com.example.ui.screens.ProfileAndMoreScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.CaduceusTheme
import com.example.ui.theme.DarkBloodRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var openAdminDeepLink by mutableStateOf(false)
    private var openNotificationsTab by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CaduceusNotificationHelper.initNotificationChannel(this)
        CaduceusAudioManager.initIfNeeded(this)
        openAdminDeepLink = isAdminDeepLink(intent)
        openNotificationsTab = intent?.getStringExtra("open_tab") == "NOTIFICATIONS"

        val repository = CaduceusRepository(this)

        setContent {
            CaduceusTheme {
                CaduceusAppRoot(
                    repository = repository,
                    launchAdminRoute = openAdminDeepLink,
                    onConsumeAdminDeepLink = { openAdminDeepLink = false },
                    launchNotificationsTab = openNotificationsTab,
                    onConsumeNotificationsTab = { openNotificationsTab = false }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        CaduceusAudioManager.startBgm(this)
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            CaduceusAudioManager.pauseBgm()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            CaduceusAudioManager.release()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isAdminDeepLink(intent)) {
            openAdminDeepLink = true
        }
        if (intent.getStringExtra("open_tab") == "NOTIFICATIONS") {
            openNotificationsTab = true
        }
    }

    private fun isAdminDeepLink(intent: Intent?): Boolean {
        val uri = intent?.data ?: return false
        val host = uri.host ?: ""
        val path = uri.path ?: ""
        return (uri.scheme.equals("caduceus", ignoreCase = true) && host.equals("admin", ignoreCase = true)) ||
            path.startsWith("/admin", ignoreCase = true)
    }
}

enum class RootNavState {
    SPLASH,
    ONBOARDING,
    AUTH,
    ADMIN_AUTH,
    MAIN_APP
}

enum class MainTab {
    HOME,
    EVENTS,
    HOROSCOPE,
    NOTIFICATIONS,
    PROFILE
}

@Composable
fun CaduceusAppRoot(
    repository: CaduceusRepository,
    launchAdminRoute: Boolean,
    onConsumeAdminDeepLink: () -> Unit,
    launchNotificationsTab: Boolean,
    onConsumeNotificationsTab: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var rootState by remember { mutableStateOf(RootNavState.SPLASH) }
    var startInAdminPortal by remember { mutableStateOf(false) }
    var currentAuthUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentAuthUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(launchAdminRoute) {
        if (launchAdminRoute) {
            onConsumeAdminDeepLink()
            if (currentAuthUser != null) {
                startInAdminPortal = true
                rootState = RootNavState.MAIN_APP
            } else {
                rootState = RootNavState.ADMIN_AUTH
            }
        }
    }

    val performSignOut: (RootNavState) -> Unit = { destinationAfterSignOut ->
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (_: Exception) {}
                Firebase.auth.signOut()
                startInAdminPortal = false
                rootState = destinationAfterSignOut
            }
        }
    }

    when (rootState) {
        RootNavState.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    val user = currentAuthUser
                    if (launchAdminRoute) {
                        onConsumeAdminDeepLink()
                        if (user != null) {
                            startInAdminPortal = true
                            rootState = RootNavState.MAIN_APP
                        } else {
                            rootState = RootNavState.ADMIN_AUTH
                        }
                    } else if (user == null) {
                        rootState = RootNavState.ONBOARDING
                    } else {
                        rootState = RootNavState.MAIN_APP
                    }
                }
            )
        }
        RootNavState.ONBOARDING -> {
            OnboardingScreen(
                onComplete = {
                    rootState = RootNavState.AUTH
                }
            )
        }
        RootNavState.AUTH -> {
            AuthAndRegistrationScreen(
                repository = repository,
                onAuthComplete = {
                    val signedInUid = Firebase.auth.currentUser?.uid
                    if (signedInUid != null) {
                        scope.launch {
                            val profile = repository.getUserProfileOnce(signedInUid)
                            val isAdmin = profile?.role == UserRole.SUPER_ADMIN.name ||
                                profile?.role == UserRole.EVENT_ADMIN.name
                            startInAdminPortal = isAdmin
                            rootState = RootNavState.MAIN_APP
                        }
                    } else {
                        startInAdminPortal = false
                        rootState = RootNavState.MAIN_APP
                    }
                },
                onOpenAdminRoute = {
                    rootState = RootNavState.ADMIN_AUTH
                }
            )
        }
        RootNavState.ADMIN_AUTH -> {
            BackHandler { rootState = RootNavState.AUTH }
            AdminSignInScreen(
                repository = repository,
                onAdminAuthenticated = {
                    startInAdminPortal = true
                    rootState = RootNavState.MAIN_APP
                },
                onUnauthorizedUser = {
                    startInAdminPortal = true
                    rootState = RootNavState.MAIN_APP
                },
                onBackToApp = {
                    rootState = RootNavState.AUTH
                }
            )
        }
        RootNavState.MAIN_APP -> {
            val user = currentAuthUser
            if (user == null) {
                AuthAndRegistrationScreen(
                    repository = repository,
                    onAuthComplete = {
                        val signedInUid = Firebase.auth.currentUser?.uid
                        if (signedInUid != null) {
                            scope.launch {
                                val profile = repository.getUserProfileOnce(signedInUid)
                                startInAdminPortal = profile?.role == UserRole.SUPER_ADMIN.name ||
                                    profile?.role == UserRole.EVENT_ADMIN.name
                                rootState = RootNavState.MAIN_APP
                            }
                        } else {
                            rootState = RootNavState.MAIN_APP
                        }
                    },
                    onOpenAdminRoute = {
                        rootState = RootNavState.ADMIN_AUTH
                    }
                )
            } else {
                val resolvedAuthEmail = user.email?.takeIf { it.isNotBlank() }
                    ?: user.providerData.firstOrNull { !it.email.isNullOrBlank() }?.email
                    ?: ""
                MainFestAppContainer(
                    authUid = user.uid,
                    authEmail = resolvedAuthEmail,
                    initialShowAdminPortal = startInAdminPortal,
                    onConsumedInitialAdminPortal = { startInAdminPortal = false },
                    launchNotificationsTab = launchNotificationsTab,
                    onConsumeNotificationsTab = onConsumeNotificationsTab,
                    repository = repository,
                    onSwitchToAdminSignIn = { performSignOut(RootNavState.ADMIN_AUTH) },
                    onSignOut = { performSignOut(RootNavState.AUTH) }
                )
            }
        }
    }
}

@Composable
fun MainFestAppContainer(
    authUid: String,
    authEmail: String,
    initialShowAdminPortal: Boolean,
    onConsumedInitialAdminPortal: () -> Unit,
    launchNotificationsTab: Boolean,
    onConsumeNotificationsTab: () -> Unit,
    repository: CaduceusRepository,
    onSwitchToAdminSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by repository.observeCurrentUser(authUid).collectAsState(initial = null)
    var locallyRefreshedProfile by remember(authUid) { mutableStateOf<CaduceusUser?>(null) }
    var isCheckingApproval by remember { mutableStateOf(false) }
    var approvalStatusMessage by remember { mutableStateOf<String?>(null) }

    var showAdminPortal by remember { mutableStateOf(initialShowAdminPortal) }
    var isEditingPersonalDetails by remember(authUid) { mutableStateOf(false) }
    var isSavingPersonalDetails by remember { mutableStateOf(false) }
    var personalDetailsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialShowAdminPortal) {
        if (initialShowAdminPortal) {
            showAdminPortal = true
            onConsumedInitialAdminPortal()
        }
    }

    // Track unique app install telemetry once authenticated
    LaunchedEffect(authUid) {
        repository.recordAppInstallIfNeeded(authUid, authEmail)
    }

    // If profile has not yet been created in Firestore, provision it (PENDING for regular users, APPROVED for Super Admin)
    val resolvedProfile = userProfile ?: locallyRefreshedProfile
    if (resolvedProfile == null) {
        LaunchedEffect(authUid) {
            repository.registerUserProfile(
                uid = authUid,
                fullName = Firebase.auth.currentUser?.displayName ?: "Caduceus Attendee",
                email = authEmail,
                phone = "",
                collegeName = "CADUCEUS 2026",
                department = "Cultural Delegate",
                studentId = "",
                zodiacSign = "Scorpio"
            ).onSuccess { created ->
                locallyRefreshedProfile = created
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AntiqueGold)
        }
        return
    }

    val currentProfile = if (userProfile != null) userProfile!! else resolvedProfile
    val isAdmin = currentProfile.role == UserRole.SUPER_ADMIN.name ||
        currentProfile.role == UserRole.EVENT_ADMIN.name

    // Ensure Super Admin owner account always holds SUPER_ADMIN + APPROVED even on pre-existing profile docs
    if (authEmail.equals(CaduceusRepository.SUPER_ADMIN_EMAIL, ignoreCase = true) &&
        (currentProfile.role != UserRole.SUPER_ADMIN.name ||
            currentProfile.approvalStatus != ApprovalStatus.APPROVED.name)
    ) {
        LaunchedEffect(currentProfile.userId, currentProfile.role, currentProfile.approvalStatus) {
            if (currentProfile.role != UserRole.SUPER_ADMIN.name) {
                repository.setUserRole(currentProfile.userId, UserRole.SUPER_ADMIN)
            }
            if (currentProfile.approvalStatus != ApprovalStatus.APPROVED.name) {
                repository.setUserApprovalStatus(currentProfile.userId, ApprovalStatus.APPROVED)
            }
        }
    }

    // Block unauthorized /admin attempts before checking user approval
    if (showAdminPortal && !isAdmin) {
        BackHandler { showAdminPortal = false }
        AdminUnauthorizedScreen(
            user = currentProfile,
            onSwitchAccount = onSwitchToAdminSignIn,
            onReturnToApp = { showAdminPortal = false }
        )
        return
    }

    // Prompt for mandatory Personal Details right after Google Sign-In (or when editing from Pending/Profile)
    val needsPersonalDetails = CaduceusRepository.shouldPromptPersonalDetails(currentProfile)
    if (needsPersonalDetails || isEditingPersonalDetails) {
        if (isEditingPersonalDetails) {
            BackHandler {
                personalDetailsError = null
                isEditingPersonalDetails = false
            }
        }
        PersonalDetailsScreen(
            user = currentProfile,
            isEditMode = isEditingPersonalDetails,
            isSaving = isSavingPersonalDetails,
            errorMessage = personalDetailsError,
            onSaveDetails = { name, college, designationVal, batchVal, rollVal ->
                CaduceusAudioManager.playTap(context)
                isSavingPersonalDetails = true
                personalDetailsError = null
                scope.launch {
                    val result = repository.updatePersonalDetails(
                        uid = authUid,
                        fullName = name,
                        collegeName = college,
                        designation = designationVal,
                        batch = batchVal,
                        rollNumber = rollVal
                    )
                    if (result.isSuccess) {
                        val refreshed = repository.getUserProfileOnce(authUid)
                        locallyRefreshedProfile = refreshed ?: currentProfile.copy(
                            fullName = name,
                            collegeName = college,
                            designation = designationVal,
                            batch = batchVal,
                            rollNumber = rollVal,
                            studentId = rollVal,
                            profileCompleted = true
                        )
                        isEditingPersonalDetails = false
                    } else {
                        personalDetailsError = result.exceptionOrNull()?.localizedMessage
                            ?: "Unable to save details right now. Please try again."
                    }
                    isSavingPersonalDetails = false
                }
            },
            onCancelEdit = if (isEditingPersonalDetails) {
                {
                    CaduceusAudioManager.playTap(context)
                    personalDetailsError = null
                    isEditingPersonalDetails = false
                }
            } else null,
            onSignOut = if (!isEditingPersonalDetails) {
                {
                    CaduceusAudioManager.playTap(context)
                    onSignOut()
                }
            } else null
        )
        return
    }

    // Gate ALL internal screens, streams, and fest notifications behind Admin Approval
    val isApproved = CaduceusRepository.isUserAllowedAccess(currentProfile) ||
        CaduceusRepository.isUserAllowedAccess(locallyRefreshedProfile)

    if (!isApproved) {
        PendingApprovalScreen(
            user = currentProfile,
            isCheckingStatus = isCheckingApproval,
            statusMessage = approvalStatusMessage,
            onCheckStatus = {
                CaduceusAudioManager.playTap(context)
                isCheckingApproval = true
                approvalStatusMessage = null
                scope.launch {
                    try {
                        val fresh = repository.getUserProfileOnce(authUid)
                        if (fresh != null) {
                            locallyRefreshedProfile = fresh
                        }
                        approvalStatusMessage = when {
                            CaduceusRepository.isUserAllowedAccess(fresh) -> null
                            fresh?.approvalStatus == ApprovalStatus.REJECTED.name ->
                                "Access is not approved. Please contact an administrator."
                            else -> "Still pending admin approval. Please check back shortly."
                        }
                    } catch (_: Exception) {
                        approvalStatusMessage = "Unable to check status right now. Please try again."
                    } finally {
                        isCheckingApproval = false
                    }
                }
            },
            onSignOut = {
                CaduceusAudioManager.playTap(context)
                onSignOut()
            },
            onEditDetails = {
                CaduceusAudioManager.playTap(context)
                personalDetailsError = null
                isEditingPersonalDetails = true
            }
        )
        return
    }

    ApprovedFestContent(
        authUid = authUid,
        currentProfile = currentProfile,
        isAdmin = isAdmin,
        showAdminPortal = showAdminPortal,
        onShowAdminPortalChange = { showAdminPortal = it },
        launchNotificationsTab = launchNotificationsTab,
        onConsumeNotificationsTab = onConsumeNotificationsTab,
        repository = repository,
        onEditPersonalDetails = {
            personalDetailsError = null
            isEditingPersonalDetails = true
        },
        onSignOut = onSignOut
    )
}

@Composable
private fun ApprovedFestContent(
    authUid: String,
    currentProfile: CaduceusUser,
    isAdmin: Boolean,
    showAdminPortal: Boolean,
    onShowAdminPortalChange: (Boolean) -> Unit,
    launchNotificationsTab: Boolean,
    onConsumeNotificationsTab: () -> Unit,
    repository: CaduceusRepository,
    onEditPersonalDetails: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val events by repository.observeEvents().collectAsState(initial = emptyList())
    val notifications by repository.observeNotifications().collectAsState(initial = emptyList())
    val venues by repository.observeVenues().collectAsState(initial = emptyList())
    val festSettings by repository.observeFestSettings().collectAsState(initial = FestSettings())
    val leaderboard by repository.observeLeaderboard().collectAsState(initial = emptyList())

    var activeTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedEvent by remember { mutableStateOf<CaduceusEvent?>(null) }
    var showDigitalPass by remember { mutableStateOf(false) }

    LaunchedEffect(launchNotificationsTab) {
        if (launchNotificationsTab) {
            activeTab = MainTab.NOTIFICATIONS
            showDigitalPass = false
            onShowAdminPortalChange(false)
            selectedEvent = null
            onConsumeNotificationsTab()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            CaduceusNotificationHelper.scheduleBackgroundSync(context)
        }
    }

    // Request Android 13+ notification permission, subscribe FCM topic, and schedule background sync ONLY for approved users
    LaunchedEffect(authUid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        CaduceusNotificationHelper.scheduleBackgroundSync(context)
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("caduceus_all")
        } catch (_: Exception) {}
    }

    // Dispatch unseen notifications as real Android mobile heads-up & lock-screen notifications
    LaunchedEffect(notifications) {
        var anyTriggered = false
        for (notif in notifications.asReversed()) {
            if (notif.id.isNotBlank() && notif.title.isNotBlank() && notif.message.isNotBlank()) {
                val triggered = CaduceusNotificationHelper.showHeadsUpNotificationIfUnseen(
                    context = context,
                    docId = notif.id,
                    title = notif.title,
                    message = notif.message,
                    targetVenue = notif.targetVenue.takeIf { it.isNotBlank() }
                )
                if (triggered) {
                    anyTriggered = true
                }
            }
        }
        if (anyTriggered) {
            CaduceusAudioManager.playAlert(context)
        }
    }

    // Gate admin-only Firestore streams behind verified Admin role to honor RBAC security rules
    val allUsers by remember(isAdmin) {
        if (isAdmin) repository.observeAllUsers() else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val appInstalls by remember(isAdmin) {
        if (isAdmin) repository.observeAppInstalls() else flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    // Admin-only initial fest seeding & legacy sample cleanup
    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Modal Overlays
    if (showDigitalPass) {
        BackHandler { showDigitalPass = false }
        DigitalPassScreen(
            user = currentProfile,
            onBack = { showDigitalPass = false }
        )
        return
    }

    if (showAdminPortal && isAdmin) {
        BackHandler { onShowAdminPortalChange(false) }
        AdminPortalScreen(
            currentUser = currentProfile,
            allUsers = allUsers,
            appInstalls = appInstalls,
            events = events,
            notifications = notifications,
            festSettings = festSettings,
            repository = repository,
            onBack = { onShowAdminPortalChange(false) }
        )
        return
    }

    if (selectedEvent != null) {
        BackHandler { selectedEvent = null }
        EventDetailScreen(
            event = selectedEvent!!,
            onBack = { selectedEvent = null }
        )
        return
    }

    // Main 5-tab Navigation Scaffold
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkObsidian,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == MainTab.HOME,
                    onClick = {
                        if (activeTab != MainTab.HOME) CaduceusAudioManager.playTap(context)
                        activeTab = MainTab.HOME
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AntiqueGold,
                        indicatorColor = DarkBloodRed,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.EVENTS,
                    onClick = {
                        if (activeTab != MainTab.EVENTS) CaduceusAudioManager.playTap(context)
                        activeTab = MainTab.EVENTS
                    },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Events") },
                    label = { Text("Events", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AntiqueGold,
                        indicatorColor = DarkBloodRed,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.HOROSCOPE,
                    onClick = {
                        if (activeTab != MainTab.HOROSCOPE) CaduceusAudioManager.playReveal(context)
                        activeTab = MainTab.HOROSCOPE
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Horoscope") },
                    label = { Text("Zodiac", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AntiqueGold,
                        indicatorColor = DarkBloodRed,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.NOTIFICATIONS,
                    onClick = {
                        if (activeTab != MainTab.NOTIFICATIONS) CaduceusAudioManager.playTap(context)
                        activeTab = MainTab.NOTIFICATIONS
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AntiqueGold,
                        indicatorColor = DarkBloodRed,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.PROFILE,
                    onClick = {
                        if (activeTab != MainTab.PROFILE) CaduceusAudioManager.playTap(context)
                        activeTab = MainTab.PROFILE
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextWhite,
                        selectedTextColor = AntiqueGold,
                        indicatorColor = DarkBloodRed,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                MainTab.HOME -> HomeScreen(
                    user = currentProfile,
                    events = events,
                    notifications = notifications,
                    festSettings = festSettings,
                    onNavigateToEvents = {
                        CaduceusAudioManager.playTap(context)
                        activeTab = MainTab.EVENTS
                    },
                    onNavigateToPass = {
                        CaduceusAudioManager.playReveal(context)
                        showDigitalPass = true
                    },
                    onNavigateToZodiac = {
                        CaduceusAudioManager.playReveal(context)
                        activeTab = MainTab.HOROSCOPE
                    },
                    onNavigateToNotifications = {
                        CaduceusAudioManager.playTap(context)
                        activeTab = MainTab.NOTIFICATIONS
                    },
                    onNavigateToAdmin = {
                        CaduceusAudioManager.playTap(context)
                        onShowAdminPortalChange(true)
                    },
                    onSelectEvent = {
                        CaduceusAudioManager.playTap(context)
                        selectedEvent = it
                    }
                )
                MainTab.EVENTS -> EventsScreen(
                    events = events,
                    onSelectEvent = { selectedEvent = it }
                )
                MainTab.HOROSCOPE -> HoroscopeScreen(
                    user = currentProfile,
                    repository = repository
                )
                MainTab.NOTIFICATIONS -> NotificationsScreen(
                    notifications = notifications,
                    onSelectNotification = { notif ->
                        val matchingEvent = events.firstOrNull { it.venueName.equals(notif.targetVenue, ignoreCase = true) }
                        if (matchingEvent != null) {
                            selectedEvent = matchingEvent
                        }
                    }
                )
                MainTab.PROFILE -> ProfileAndMoreScreen(
                    user = currentProfile,
                    festSettings = festSettings,
                    venues = venues,
                    leaderboard = leaderboard,
                    onOpenDigitalPass = {
                        CaduceusAudioManager.playReveal(context)
                        showDigitalPass = true
                    },
                    onOpenAdminPortal = {
                        CaduceusAudioManager.playTap(context)
                        onShowAdminPortalChange(true)
                    },
                    onSignOut = {
                        CaduceusAudioManager.playTap(context)
                        onSignOut()
                    },
                    onEditPersonalDetails = onEditPersonalDetails
                )
            }
        }
    }
}
