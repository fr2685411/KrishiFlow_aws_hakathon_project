package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthUiState
import com.example.data.auth.KrishiAuthManager
import com.example.ui.AppPersonaMode
import com.example.ui.components.SecureAuthDialog
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGoogleSignInIntegration() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val authManager = KrishiAuthManager(app)

        val result = authManager.signInWithGoogle(
            role = AppPersonaMode.KISAN_FARMER,
            accountName = "Ajrudeen Kureshi",
            accountEmail = "ajrudeenkureshi2003@gmail.com"
        )

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("Ajrudeen Kureshi", user?.displayName)
        assertEquals("ajrudeenkureshi2003@gmail.com", user?.email)
        assertEquals(AppPersonaMode.KISAN_FARMER, user?.role)
        assertEquals("GOOGLE", user?.authProvider)

        val state = authManager.authState.value
        assertTrue(state is AuthUiState.Authenticated)
    }

    @Test
    fun testPhoneNumberOtpLoginFlow() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val authManager = KrishiAuthManager(app)

        // Request OTP
        val otpResult = authManager.requestPhoneOtp("+91 98220 14829")
        assertTrue(otpResult.isSuccess)
        val verificationId = otpResult.getOrNull()
        assertNotNull(verificationId)

        val stateAfterOtp = authManager.authState.value
        assertTrue(stateAfterOtp is AuthUiState.OtpSent)
        val demoOtp = (stateAfterOtp as AuthUiState.OtpSent).demoOtp

        // Verify OTP
        val verifyResult = authManager.verifyPhoneOtp(
            verificationId = verificationId!!,
            otpCode = demoOtp,
            role = AppPersonaMode.VYAPAR_MANDI,
            phoneNumber = "+91 98220 14829"
        )

        assertTrue(verifyResult.isSuccess)
        val user = verifyResult.getOrNull()
        assertNotNull(user)
        assertEquals("+91 98220 14829", user?.phoneNumber)
        assertEquals(AppPersonaMode.VYAPAR_MANDI, user?.role)
        assertEquals("PHONE", user?.authProvider)

        val finalState = authManager.authState.value
        assertTrue(finalState is AuthUiState.Authenticated)
    }

    @Test
    fun testSecureAuthDialogRendersGoogleAndPhoneTabs() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = KrishiViewModel(app)

        var dialogDismissed = false

        composeTestRule.setContent {
            SecureAuthDialog(
                uiState = KrishiUiState(),
                viewModel = viewModel,
                onDismiss = { dialogDismissed = true }
            )
        }

        // Check tabs exist
        composeTestRule.onNodeWithTag("auth_tab_phone").assertExists()
        composeTestRule.onNodeWithTag("auth_tab_google").assertExists()
        composeTestRule.onNodeWithTag("auth_tab_email").assertExists()
        composeTestRule.onNodeWithTag("auth_tab_demo").assertExists()

        // Check Phone inputs
        composeTestRule.onNodeWithTag("input_auth_phone").assertExists()
        composeTestRule.onNodeWithTag("send_phone_otp_button").assertExists()

        // Switch to Google Tab
        composeTestRule.onNodeWithTag("auth_tab_google").performClick()
        composeTestRule.waitForIdle()

        // Check Google Sign-in elements
        composeTestRule.onNodeWithTag("input_google_email").assertExists()
        composeTestRule.onNodeWithTag("google_signin_button").assertExists()

        // Click close
        composeTestRule.onNodeWithTag("auth_dialog_close_button").performClick()
        assertTrue(dialogDismissed)
    }
}
