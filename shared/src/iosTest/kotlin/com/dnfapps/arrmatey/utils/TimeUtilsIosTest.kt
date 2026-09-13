package com.dnfapps.arrmatey.utils

import com.dnfapps.arrmatey.isDebug
import kotlin.test.Test
import kotlin.test.assertTrue

class TimeUtilsIosTest : TimeUtilsContract()

class IosPlatformTest {
    @Test
    fun testScreenDensityIsPositive() {
        assertTrue(screenDensity > 0f, "UIScreen scale should never be zero")
    }

    @Test
    fun testIsDebugReturnsTrueForTestBinary() {
        assertTrue(isDebug())
    }
}
