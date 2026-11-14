package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusUser
import com.example.data.model.UserRole
import com.example.data.repository.CaduceusRepository
import com.example.data.util.CaduceusAudioManager
import com.example.ui.screens.PendingApprovalScreen
import com.example.ui.theme.CaduceusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApprovalGateAndAudioTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `new student account defaults to PENDING and is blocked`() {
        val role = CaduceusRepository.resolveRole("student@example.com", null)
        val status = CaduceusRepository.resolveApprovalStatus(role, null)
        assertEquals(UserRole.STUDENT.name, role)
        assertEquals(ApprovalStatus.PENDING.name, status)

        val user = CaduceusUser(
            userId = "u1",
            fullName = "Test Student",
            email = "student@example.com",
            role = role,
            approvalStatus = status
        )
        assertFalse(CaduceusRepository.isUserAllowedAccess(user))
    }

    @Test
    fun `pending and approved statuses persist across logout and re-login`() {
        val pendingOnRelogin = CaduceusRepository.resolveApprovalStatus(
            role = UserRole.STUDENT.name,
            existingApprovalStatus = ApprovalStatus.PENDING.name
        )
        assertEquals(ApprovalStatus.PENDING.name, pendingOnRelogin)

        val approvedOnRelogin = CaduceusRepository.resolveApprovalStatus(
            role = UserRole.STUDENT.name,
            existingApprovalStatus = ApprovalStatus.APPROVED.name
        )
        assertEquals(ApprovalStatus.APPROVED.name, approvedOnRelogin)

        val rejectedOnRelogin = CaduceusRepository.resolveApprovalStatus(
            role = UserRole.STUDENT.name,
            existingApprovalStatus = ApprovalStatus.REJECTED.name
        )
        assertEquals(ApprovalStatus.REJECTED.name, rejectedOnRelogin)
    }

    @Test
    fun `approved student and admins are allowed access while rejected is blocked`() {
        val approvedUser = CaduceusUser(
            userId = "u2",
            role = UserRole.STUDENT.name,
            approvalStatus = ApprovalStatus.APPROVED.name
        )
        assertTrue(CaduceusRepository.isUserAllowedAccess(approvedUser))

        val rejectedUser = approvedUser.copy(approvalStatus = ApprovalStatus.REJECTED.name)
        assertFalse(CaduceusRepository.isUserAllowedAccess(rejectedUser))

        val superAdminRole = CaduceusRepository.resolveRole(CaduceusRepository.SUPER_ADMIN_EMAIL, null)
        val superAdminStatus = CaduceusRepository.resolveApprovalStatus(superAdminRole, null)
        assertEquals(UserRole.SUPER_ADMIN.name, superAdminRole)
        assertEquals(ApprovalStatus.APPROVED.name, superAdminStatus)
        assertTrue(
            CaduceusRepository.isUserAllowedAccess(
                CaduceusUser(userId = "admin", role = superAdminRole, approvalStatus = superAdminStatus)
            )
        )
    }

    @Test
    fun `pending approval screen renders status and triggers refresh and sign out`() {
        var checkClicks = 0
        var signOutClicks = 0
        val pendingUser = CaduceusUser(
            userId = "u3",
            fullName = "Aarav Sen",
            email = "aarav@example.com",
            festPassId = "CAD26-U3-1234",
            role = UserRole.STUDENT.name,
            approvalStatus = ApprovalStatus.PENDING.name
        )

        composeRule.setContent {
            CaduceusTheme {
                PendingApprovalScreen(
                    user = pendingUser,
                    isCheckingStatus = false,
                    statusMessage = null,
                    onCheckStatus = { checkClicks++ },
                    onSignOut = { signOutClicks++ }
                )
            }
        }

        composeRule.onNodeWithTag("pending_approval_screen").assertIsDisplayed()
        composeRule.onNodeWithText("PENDING ADMIN APPROVAL").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Aarav Sen").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("aarav@example.com").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("pending_check_status_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("pending_sign_out_button").performScrollTo().performClick()

        assertEquals(1, checkClicks)
        assertEquals(1, signOutClicks)
    }

    @Test
    fun `rejected user sees access not approved screen`() {
        val rejectedUser = CaduceusUser(
            userId = "u4",
            fullName = "Revoked Attendee",
            email = "revoked@example.com",
            role = UserRole.STUDENT.name,
            approvalStatus = ApprovalStatus.REJECTED.name
        )

        composeRule.setContent {
            CaduceusTheme {
                PendingApprovalScreen(
                    user = rejectedUser,
                    isCheckingStatus = false,
                    statusMessage = "Access is not approved.",
                    onCheckStatus = {},
                    onSignOut = {}
                )
            }
        }

        composeRule.onNodeWithText("ACCESS NOT APPROVED").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Access is not approved.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `audio manager boosts effective BGM volume by 15 to 20 percent without clipping and toggles safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val previousVolume = 0.08f
        val newVolume = CaduceusAudioManager.BGM_VOLUME
        val increaseRatio = (newVolume - previousVolume) / previousVolume

        assertTrue("Expected +15% to +20% gain boost, got $increaseRatio", increaseRatio in 0.15f..0.21f)
        assertTrue("BGM gain must stay safely below clipping", newVolume < 0.25f)

        val initial = CaduceusAudioManager.isAudioEnabled(context)
        val toggled = CaduceusAudioManager.toggleAudio(context)
        assertEquals(!initial, toggled)
        val restored = CaduceusAudioManager.toggleAudio(context)
        assertEquals(initial, restored)

        CaduceusAudioManager.startBgm(context)
        CaduceusAudioManager.startBgm(context)
        CaduceusAudioManager.pauseBgm()
        CaduceusAudioManager.release()
    }
}
