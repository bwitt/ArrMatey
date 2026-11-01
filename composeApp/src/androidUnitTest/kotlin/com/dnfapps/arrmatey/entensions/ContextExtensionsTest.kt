package com.dnfapps.arrmatey.entensions

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class ContextExtensionsTest {
    private val context: Application = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        shadowOf(context).checkActivities(true)
    }

    @Test
    fun `openLink without any handler shows a toast instead of crashing`() {
        context.openLink("https://example.com")

        assertNull(shadowOf(context).nextStartedActivity)
        assertEquals("No app available to open this", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `openAppSettings without a settings app shows a toast instead of crashing`() {
        context.openAppSettings()

        assertEquals("No app available to open this", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `openLink falls back to a view intent handler`() {
        val browser = ComponentName("com.example.browser", "com.example.browser.Main")
        shadowOf(context.packageManager).apply {
            addActivityIfNotPresent(browser)
            addIntentFilterForActivity(
                browser,
                IntentFilter(Intent.ACTION_VIEW).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addDataScheme("https")
                },
            )
        }

        context.openLink("https://example.com")

        assertEquals("https://example.com", shadowOf(context).nextStartedActivity?.dataString)
    }
}
