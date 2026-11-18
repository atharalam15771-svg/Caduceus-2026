package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.model.ApprovalStatus
import com.example.data.model.CaduceusUser
import com.example.data.model.UserRole
import com.example.data.repository.CaduceusRepository
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PendingApprovalScreen
import com.example.ui.screens.PersonalDetailsScreen
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
class PersonalDetailsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `shouldPromptPersonalDetails returns true only for incomplete students`() {
        assertFalse(CaduceusRepository.shouldPromptPersonalDetails(null))

        val incompleteStudent = CaduceusUser(
            userId = "s1",
            role = UserRole.STUDENT.name,
            profileCompleted = false
        )
        assertTrue(CaduceusRepository.shouldPromptPersonalDetails(incompleteStudent))

        val completedStudent = incompleteStudent.copy(profileCompleted = true)
        assertFalse(CaduceusRepository.shouldPromptPersonalDetails(completedStudent))

        val superAdmin = incompleteStudent.copy(role = UserRole.SUPER_ADMIN.name)
        assertFalse(CaduceusRepository.shouldPromptPersonalDetails(superAdmin))

        val eventAdmin = incompleteStudent.copy(role = UserRole.EVENT_ADMIN.name)
        assertFalse(CaduceusRepository.shouldPromptPersonalDetails(eventAdmin))
    }

    @Test
    fun `mandatory personal details has no skip or student toggle and validates Name College Designation Batch Roll in order`() {
        var savedName = ""
        var savedCollege = ""
        var savedDesignation = ""
        var savedBatch = ""
        var savedRoll = ""

        val user = CaduceusUser(
            userId = "u_stu",
            fullName = "Caduceus Attendee",
            email = "student@dmgmch.edu"
        )

        composeRule.setContent {
            CaduceusTheme {
                PersonalDetailsScreen(
                    user = user,
                    onSaveDetails = { name, college, designation, batch, roll ->
                        savedName = name
                        savedCollege = college
                        savedDesignation = designation
                        savedBatch = batch
                        savedRoll = roll
                    }
                )
            }
        }

        // Neither skip button nor student toggle exists
        composeRule.onNodeWithTag("personal_skip_button").assertDoesNotExist()
        composeRule.onNodeWithTag("personal_student_yes").assertDoesNotExist()
        composeRule.onNodeWithTag("personal_student_no").assertDoesNotExist()

        // Step-by-step compulsory validation in exact field order
        composeRule.onNodeWithTag("personal_save_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Please enter your name.").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("personal_name_input").performScrollTo().performTextInput("Rohan Das")
        composeRule.onNodeWithTag("personal_save_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Please enter your college name.").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("personal_college_input").performScrollTo().performTextInput("DMGMCH Purulia")
        composeRule.onNodeWithTag("personal_save_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Please enter your designation.").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("personal_designation_input").performScrollTo().performTextInput("MBBS Student")
        composeRule.onNodeWithTag("personal_save_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Please enter your batch.").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("personal_batch_input").performScrollTo().performTextInput("2023")
        composeRule.onNodeWithTag("personal_save_button").performScrollTo().performClick()
        composeRule.onNodeWithText("Please enter your roll number.").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("personal_roll_input").performScrollTo().performTextInput("42")

        // Verify exact top-to-bottom declaration order inside personal_details_card:
        // Name -> College Name -> Designation -> Batch -> Roll Number
        fun collectTags(node: androidx.compose.ui.semantics.SemanticsNode): List<String> {
            val own = node.config.getOrElseNullable(androidx.compose.ui.semantics.SemanticsProperties.TestTag) { null }
            return listOfNotNull(own) + node.children.flatMap { collectTags(it) }
        }
        val orderedTags = collectTags(composeRule.onNodeWithTag("personal_details_card").fetchSemanticsNode())
        val expectedOrder = listOf(
            "personal_name_input",
            "personal_college_input",
            "personal_designation_input",
            "personal_batch_input",
            "personal_roll_input"
        )
        val indices = expectedOrder.map { orderedTags.indexOf(it) }
        assertTrue("Expected all 5 fields in card, found: $orderedTags", indices.all { it >= 0 })
        assertEquals(indices.sorted(), indices)

        composeRule.onNodeWithTag("personal_save_button").performScrollTo().performClick()

        assertEquals("Rohan Das", savedName)
        assertEquals("DMGMCH Purulia", savedCollege)
        assertEquals("MBBS Student", savedDesignation)
        assertEquals("2023", savedBatch)
        assertEquals("42", savedRoll)
    }

    @Test
    fun `pending approval screen renders college designation batch roll and edit button`() {
        var editClicks = 0
        val pendingStudent = CaduceusUser(
            userId = "u_pend",
            fullName = "Arjun Roy",
            email = "arjun@example.com",
            collegeName = "DMGMCH",
            designation = "Intern",
            batch = "2021",
            rollNumber = "17",
            festPassId = "CAD26-PEND-1010",
            role = UserRole.STUDENT.name,
            approvalStatus = ApprovalStatus.PENDING.name
        )

        composeRule.setContent {
            CaduceusTheme {
                PendingApprovalScreen(
                    user = pendingStudent,
                    isCheckingStatus = false,
                    statusMessage = null,
                    onCheckStatus = {},
                    onSignOut = {},
                    onEditDetails = { editClicks++ }
                )
            }
        }

        composeRule.onNodeWithText("College: DMGMCH").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Designation: Intern").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Batch: 2021  •  Roll No: 17").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("pending_edit_details_button").performScrollTo().performClick()
        assertEquals(1, editClicks)
    }

    @Test
    fun `onboarding shows Created by MD ATHAR ALAM before sign in`() {
        composeRule.setContent {
            CaduceusTheme {
                OnboardingScreen(onComplete = {})
            }
        }

        composeRule.onNodeWithTag("onboarding_creator_credit").assertIsDisplayed()
        composeRule.onNodeWithText("Created by MD ATHAR ALAM").assertIsDisplayed()
    }
}
