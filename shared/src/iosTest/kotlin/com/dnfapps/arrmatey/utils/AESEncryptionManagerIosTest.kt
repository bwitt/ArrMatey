@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.dnfapps.arrmatey.utils

import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.Foundation.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AESEncryptionManagerIosTest {
    private val manager = AESEncryptionManager()
    private val key: NSData =
        ByteArray(32) { it.toByte() }.usePinned {
            NSData.create(bytes = it.addressOf(0), length = 32u)
        }

    @Test
    fun encryptedValuesRoundTripWithVersionPrefix() {
        val encrypted = assertNotNull(manager.encryptWithKey("sk_live_1234567890abcdef", key))

        assertTrue(encrypted.startsWith("v2:"))
        assertEquals("sk_live_1234567890abcdef", manager.decryptWithKey(encrypted, key))
    }

    @Test
    fun encryptingTheSameValueTwiceUsesDifferentIvs() {
        val first = manager.encryptWithKey("same-secret", key)
        val second = manager.encryptWithKey("same-secret", key)

        assertNotEquals(first, second)
    }

    @Test
    fun legacyNilIvValuesStillDecrypt() {
        val plain = (("héllo wörld — 日本語" as Any) as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
        val legacy = manager.crypt(kCCEncrypt, plain, key, null)!!.base64EncodedStringWithOptions(0u)

        assertEquals("héllo wörld — 日本語", manager.decryptWithKey(legacy, key))
    }
}
