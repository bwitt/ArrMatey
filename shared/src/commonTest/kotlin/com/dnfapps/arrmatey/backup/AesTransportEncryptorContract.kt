package com.dnfapps.arrmatey.backup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// Abstract so each platform supplies its own runner; Android needs Robolectric for android.util.Base64.
abstract class AesTransportEncryptorContract {
    private val encryptor = AesTransportEncryptor()
    private val password = "correct horse battery staple"

    @Test
    fun testRoundTripReturnsOriginalPlaintext() {
        val plaintext = """{"instances":[{"label":"Sonarr","url":"http://localhost:8989"}]}"""

        val encrypted = encryptor.encrypt(plaintext, password)
        assertNotEquals(plaintext, encrypted)

        assertEquals(plaintext, encryptor.decrypt(encrypted, password))
    }

    @Test
    fun testEncryptIsSaltedSoOutputDiffersEachCall() {
        val plaintext = "same input"

        val first = encryptor.encrypt(plaintext, password)
        val second = encryptor.encrypt(plaintext, password)

        assertNotEquals(first, second, "random salt/IV must produce different ciphertext")
        assertEquals(plaintext, encryptor.decrypt(first, password))
        assertEquals(plaintext, encryptor.decrypt(second, password))
    }

    @Test
    fun testDecryptWithWrongPasswordDoesNotReturnPlaintext() {
        val plaintext = "sensitive api key"

        val encrypted = encryptor.encrypt(plaintext, password)

        assertNotEquals(plaintext, encryptor.decrypt(encrypted, "wrong password"))
    }

    @Test
    fun testDecryptWithMalformedInputReturnsEmptyString() {
        assertEquals("", encryptor.decrypt("not-base64-at-all!!", password))
    }

    @Test
    fun testDecryptWithTruncatedPayloadReturnsEmptyString() {
        // Shorter than the salt + IV header, so there is nothing to decrypt
        assertEquals("", encryptor.decrypt("AAAA", password))
    }

    @Test
    fun testRoundTripHandlesUnicodeInput() {
        val unicode = "Björk – 日本語 – ☠"

        assertEquals(unicode, encryptor.decrypt(encryptor.encrypt(unicode, password), password))
    }

    @Test
    fun testRoundTripHandlesEmptyInput() {
        assertEquals("", encryptor.decrypt(encryptor.encrypt("", password), password))
    }

    @Test
    fun testEncryptedPayloadDoesNotLeakPlaintext() {
        val plaintext = "my-secret-api-key"

        val encrypted = encryptor.encrypt(plaintext, password)

        assertTrue(encrypted.isNotEmpty())
        assertTrue(!encrypted.contains(plaintext), "ciphertext must not embed the plaintext")
    }
}
