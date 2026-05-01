package nl.jkool.beerxmlviewer

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FtpDownloadButtonComposeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainDownloadButton_isDisabledWhenSavedFtpSettingsAreInvalid() {
        renderMainWithSettings(site = "", path = "/", username = "user", password = "password")

        composeRule.onNodeWithTag("mainFtpDownloadButton").assertIsNotEnabled()
    }

    @Test
    fun mainDownloadButton_isEnabledWhenSavedFtpSettingsAreValid() {
        renderMainWithSettings(site = "ftp://example.com", path = "/", username = "user", password = "password")

        composeRule.onNodeWithTag("mainFtpDownloadButton").assertIsEnabled()
    }

    @Test
    fun settingsDownloadButton_updatesEnabledStateWhenFtpSettingsBecomeValid() {
        renderSettingsWithSettings(site = "", path = "/", username = "", password = "")

        composeRule.onNodeWithTag("settingsFtpDownloadButton").assertIsNotEnabled()

        composeRule.onNodeWithTag("ftpSiteField").performTextInput("ftp://example.com")
        composeRule.onNodeWithTag("ftpUsernameField").performTextInput("user")
        composeRule.onNodeWithTag("ftpPasswordField").performTextInput("password")

        composeRule.onNodeWithTag("settingsFtpDownloadButton").assertIsEnabled()
    }

    private fun renderMainWithSettings(
        site: String,
        path: String,
        username: String,
        password: String
    ) {
        composeRule.runOnUiThread {
            val activity = composeRule.activity
            val context = activity.applicationContext
            storeSettings(context, site, path, username, password, fullInfo = false)
            activity.setContent { Main(activity, context) }
        }
        composeRule.waitForIdle()
    }

    private fun renderSettingsWithSettings(
        site: String,
        path: String,
        username: String,
        password: String
    ) {
        composeRule.runOnUiThread {
            val activity = composeRule.activity
            val context = activity.applicationContext
            storeSettings(context, site, path, username, password, fullInfo = false)
            activity.setContent { Settings(activity, context) }
        }
        composeRule.waitForIdle()
    }
}
