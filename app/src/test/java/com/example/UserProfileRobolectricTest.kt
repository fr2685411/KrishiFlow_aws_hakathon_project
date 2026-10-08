package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserRole
import com.example.ui.screens.UserProfileScreen
import com.example.ui.viewmodel.KrishiViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserProfileRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testUserProfileScreenRendersAndSwitchesRoles() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = KrishiViewModel(app)

        var backClicked = false

        composeTestRule.setContent {
            UserProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { backClicked = true }
            )
        }

        // Verify chips for professions exist
        composeTestRule.onNodeWithTag("role_chip_farmer").assertExists()
        composeTestRule.onNodeWithTag("role_chip_vendor").assertExists()
        composeTestRule.onNodeWithTag("role_chip_mandi_manager").assertExists()
        composeTestRule.onNodeWithTag("role_chip_regional_manager").assertExists()

        // Switch to Vendor role
        composeTestRule.onNodeWithTag("role_chip_vendor").performClick()
        composeTestRule.waitForIdle()

        // Switch to Mandi Manager role
        composeTestRule.onNodeWithTag("role_chip_mandi_manager").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Farmer role
        composeTestRule.onNodeWithTag("role_chip_farmer").performClick()
        composeTestRule.waitForIdle()

        // Toggle into Edit Mode using header edit button
        composeTestRule.onNodeWithTag("toggle_edit_mode_button").performClick()
        composeTestRule.waitForIdle()

        // Verify editable fields exist
        composeTestRule.onNodeWithTag("input_farmer_land_acres").assertExists()
        composeTestRule.onNodeWithTag("input_farmer_upi").assertExists()
        composeTestRule.onNodeWithTag("save_profile_firestore_button").assertExists()

        // Click back button
        composeTestRule.onNodeWithTag("profile_back_button").performClick()
        assert(backClicked)
    }
}
