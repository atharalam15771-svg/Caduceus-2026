package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CaduceusUser
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

@Composable
fun PersonalDetailsScreen(
    user: CaduceusUser,
    isEditMode: Boolean = false,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onSaveDetails: (
        fullName: String,
        collegeName: String,
        designation: String,
        batch: String,
        rollNumber: String
    ) -> Unit,
    onCancelEdit: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null
) {
    val initialName = remember(user.userId, user.fullName) {
        if (user.fullName == "Caduceus Attendee") "" else user.fullName
    }
    val initialCollege = remember(user.userId, user.collegeName) {
        if (user.collegeName == "CADUCEUS 2026" || user.collegeName == "Non-Student / Guest") "" else user.collegeName
    }
    val initialRoll = remember(user.userId, user.rollNumber, user.studentId) {
        user.rollNumber.ifBlank { user.studentId }
    }

    var fullName by remember(user.userId) { mutableStateOf(initialName) }
    var collegeName by remember(user.userId) { mutableStateOf(initialCollege) }
    var designation by remember(user.userId) { mutableStateOf(user.designation) }
    var batch by remember(user.userId) { mutableStateOf(user.batch) }
    var rollNumber by remember(user.userId) { mutableStateOf(initialRoll) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite,
        focusedBorderColor = AntiqueGold,
        unfocusedBorderColor = BorderDark,
        focusedLabelColor = AntiqueGold,
        unfocusedLabelColor = TextLightGrey,
        cursorColor = AntiqueGold,
        focusedContainerColor = DarkObsidian,
        unfocusedContainerColor = DarkObsidian
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBloodRed.copy(alpha = 0.22f), VoidBlack, VoidBlack)
                )
            )
            .safeDrawingPadding()
            .testTag("personal_details_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.caduceus_logo),
                contentDescription = "CADUCEUS 2026 Official Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, AntiqueGold, CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = DarkBloodRed.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEditMode) "ATTENDEE PROFILE" else "MANDATORY ATTENDEE DETAILS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AntiqueGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isEditMode) "UPDATE PERSONAL DETAILS" else "COMPLETE YOUR PROFILE",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
                    color = TextWhite,
                    letterSpacing = 1.2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEditMode) {
                    "Update your personal details shown on your digital pass and verification record."
                } else {
                    "Please fill in all required personal details below to continue to verification and your Digital Fest Pass."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextLightGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("personal_details_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(AntiqueGold, BorderDark)),
                    width = 1.2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            localValidationError = null
                        },
                        label = { Text("Name *") },
                        placeholder = { Text("Enter your full name", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name",
                                tint = AntiqueGold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_name_input")
                    )

                    // 2. College Name
                    OutlinedTextField(
                        value = collegeName,
                        onValueChange = {
                            collegeName = it
                            localValidationError = null
                        },
                        label = { Text("College Name *") },
                        placeholder = { Text("e.g. Deben Mahata Govt. Medical College", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "College Name",
                                tint = AntiqueGold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_college_input")
                    )

                    // 3. Designation
                    OutlinedTextField(
                        value = designation,
                        onValueChange = {
                            designation = it
                            localValidationError = null
                        },
                        label = { Text("Designation *") },
                        placeholder = { Text("e.g. MBBS Student, Intern, Doctor, Faculty", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Designation",
                                tint = AntiqueGold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_designation_input")
                    )

                    // 4. Batch
                    OutlinedTextField(
                        value = batch,
                        onValueChange = {
                            batch = it
                            localValidationError = null
                        },
                        label = { Text("Batch *") },
                        placeholder = { Text("e.g. 2023", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Batch",
                                tint = AntiqueGold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_batch_input")
                    )

                    // 5. Roll Number
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = {
                            rollNumber = it
                            localValidationError = null
                        },
                        label = { Text("Roll Number *") },
                        placeholder = { Text("e.g. 42", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = "Roll Number",
                                tint = AntiqueGold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_roll_input")
                    )

                    val activeError = localValidationError ?: errorMessage
                    if (!activeError.isNullOrBlank()) {
                        Surface(
                            color = PrimaryCrimson.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = activeError,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PrimaryCrimson,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("personal_error_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            val trimmedName = fullName.trim()
                            val trimmedCollege = collegeName.trim()
                            val trimmedDesignation = designation.trim()
                            val trimmedBatch = batch.trim()
                            val trimmedRoll = rollNumber.trim()

                            val validationMsg = when {
                                trimmedName.isBlank() -> "Please enter your name."
                                trimmedCollege.isBlank() -> "Please enter your college name."
                                trimmedDesignation.isBlank() -> "Please enter your designation."
                                trimmedBatch.isBlank() -> "Please enter your batch."
                                trimmedRoll.isBlank() -> "Please enter your roll number."
                                else -> null
                            }

                            if (validationMsg != null) {
                                localValidationError = validationMsg
                            } else {
                                localValidationError = null
                                onSaveDetails(
                                    trimmedName,
                                    trimmedCollege,
                                    trimmedDesignation,
                                    trimmedBatch,
                                    trimmedRoll
                                )
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("personal_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCrimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isEditMode) "SAVE CHANGES" else "SUBMIT & CONTINUE",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (isEditMode && onCancelEdit != null) {
                        OutlinedButton(
                            onClick = onCancelEdit,
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("personal_cancel_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderDark)
                        ) {
                            Text(
                                text = "CANCEL",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = AntiqueGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            if (!isEditMode && onSignOut != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSignOut,
                    enabled = !isSaving,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("personal_sign_out_button")
                ) {
                    Text(
                        text = "Sign out or switch Google account",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextLightGrey)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
