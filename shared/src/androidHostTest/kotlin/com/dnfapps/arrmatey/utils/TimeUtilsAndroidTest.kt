package com.dnfapps.arrmatey.utils

import android.content.Context
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TimeUtilsAndroidTest : TimeUtilsContract() {
    // is24Hour() resolves DateHelper's Context through global Koin
    @Before
    fun startKoinWithContext() {
        startKoin {
            modules(module { single<Context> { RuntimeEnvironment.getApplication() } })
        }
    }

    @After
    fun tearDownKoin() {
        stopKoin()
    }
}
