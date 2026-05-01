package nl.jkool.beerxmlviewer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FtpDownloadButtonComposeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestMainActivity>()

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
    fun mainDownloadButton_isDisabledAndProgressIsShownWhileDownloading() {
        renderMainWithSettings(
            site = "ftp://example.com",
            path = "/",
            username = "user",
            password = "password",
            ftpStatus = FtpDownloadStatus.Running(
                message = "Downloading hops.xml",
                currentFile = "hops.xml"
            )
        )

        composeRule.onNodeWithTag("mainFtpDownloadButton").assertIsNotEnabled()
        composeRule.onNodeWithTag("ftpDownloadStatusBanner").assertIsDisplayed()
        composeRule.onNodeWithTag("ftpDownloadProgress").assertIsDisplayed()
    }

    @Test
    fun mainShowsInlineSuccessBannerAfterDownloadCompletes() {
        val message = composeRule.activity.getString(R.string.ftp_download_complete_message)
        renderMainWithSettings(
            site = "ftp://example.com",
            path = "/",
            username = "user",
            password = "password",
            ftpStatus = FtpDownloadStatus.Success(message)
        )

        composeRule.onNodeWithTag("ftpDownloadStatusBanner").assertIsDisplayed()
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun settingsDownloadButton_updatesEnabledStateWhenFtpSettingsBecomeValid() {
        renderSettingsWithSettings(site = "", path = "/", username = "", password = "")

        composeRule.onNodeWithTag("settingsFtpDownloadButton").assertIsNotEnabled()

        composeRule.onNodeWithTag("ftpSiteField").performScrollTo().performTextInput("ftp://example.com")
        composeRule.onNodeWithTag("ftpUsernameField").performScrollTo().performTextInput("user")
        composeRule.onNodeWithTag("ftpPasswordField").performScrollTo().performTextInput("password")

        composeRule.onNodeWithTag("settingsFtpDownloadButton").performScrollTo().assertIsEnabled()
    }

    private fun renderMainWithSettings(
        site: String,
        path: String,
        username: String,
        password: String,
        ftpStatus: FtpDownloadStatus = FtpDownloadStatus.Idle
    ) {
        val activity = composeRule.activity
        val context = activity.applicationContext
        storeSettings(context, site, path, username, password, fullInfo = false)
        composeRule.setContent {
            Main(activity, context, initialFtpDownloadStatus = ftpStatus)
        }
        composeRule.waitForIdle()
    }

    private fun renderSettingsWithSettings(
        site: String,
        path: String,
        username: String,
        password: String
    ) {
        val activity = composeRule.activity
        val context = activity.applicationContext
        storeSettings(context, site, path, username, password, fullInfo = false)
        composeRule.setContent {
            Settings(activity, context)
        }
        composeRule.waitForIdle()
    }
}
