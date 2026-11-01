@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.dnfapps.arrmatey.utils

import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class AESEncryptionManager : EncryptionManager {
    private val keyAccount = "com.dnfapps.arrmatey.encryption.key"

    @OptIn(BetaInteropApi::class)
    private fun getOrGenerateKey(): NSData? = memScoped {
        val query =
            NSDictionary.dictionaryWithObjectsAndKeys(
                kSecClassGenericPassword.toAny(),
                kSecClass.toAny(),
                (keyAccount as Any as NSString),
                kSecAttrAccount.toAny(),
                kCFBooleanTrue.toAny(),
                kSecReturnData.toAny(),
                kSecMatchLimitOne.toAny(),
                kSecMatchLimit.toAny(),
                null,
            )

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query as Any? as CFDictionaryRef?, result.ptr)

        if (status == errSecSuccess) {
            return CFBridgingRelease(result.value) as? NSData
        }

        // Generate new key if not found
        val newKey = NSMutableData.create(length = 32u) ?: return null
        val statusRandom = SecRandomCopyBytes(kSecRandomDefault, 32u, newKey.mutableBytes)
        if (statusRandom != errSecSuccess) return null

        val addQuery =
            NSDictionary.dictionaryWithObjectsAndKeys(
                kSecClassGenericPassword.toAny(),
                kSecClass.toAny(),
                (keyAccount as Any as NSString),
                kSecAttrAccount.toAny(),
                newKey,
                kSecValueData.toAny(),
                kSecAttrAccessibleAfterFirstUnlock.toAny(),
                kSecAttrAccessible.toAny(),
                null,
            )

        SecItemAdd(addQuery as Any? as CFDictionaryRef?, null)
        return newKey
    }

    override fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        return try {
            val key = getOrGenerateKey() ?: return plainText
            encryptWithKey(plainText, key) ?: plainText
        } catch (e: Exception) {
            plainText
        }
    }

    override fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return encryptedText
        return try {
            val key = getOrGenerateKey() ?: return encryptedText
            decryptWithKey(encryptedText, key) ?: encryptedText
        } catch (e: Exception) {
            encryptedText
        }
    }

    @OptIn(BetaInteropApi::class)
    internal fun encryptWithKey(
        plainText: String,
        key: NSData,
    ): String? {
        val data = (plainText as Any as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return null
        val iv = NSMutableData.create(length = IV_LENGTH) ?: return null
        if (SecRandomCopyBytes(kSecRandomDefault, IV_LENGTH, iv.mutableBytes) != errSecSuccess) return null
        val cipherText = crypt(kCCEncrypt, data, key, iv) ?: return null

        val combined = NSMutableData()
        combined.appendData(iv)
        combined.appendData(cipherText)
        return V2_PREFIX + combined.base64EncodedStringWithOptions(0u)
    }

    @OptIn(BetaInteropApi::class)
    internal fun decryptWithKey(
        encryptedText: String,
        key: NSData,
    ): String? {
        val isV2 = encryptedText.startsWith(V2_PREFIX)
        val data = NSData.create(base64EncodedString = encryptedText.removePrefix(V2_PREFIX), options = 0u) ?: return null
        val decryptedData =
            if (isV2) {
                if (data.length <= IV_LENGTH) return null
                val iv = data.subdataWithRange(NSMakeRange(0u, IV_LENGTH))
                val cipherText = data.subdataWithRange(NSMakeRange(IV_LENGTH, data.length - IV_LENGTH))
                crypt(kCCDecrypt, cipherText, key, iv)
            } else {
                // Values written before v2 used a nil (all-zero) IV.
                crypt(kCCDecrypt, data, key, null)
            } ?: return null
        return NSString.create(data = decryptedData, encoding = NSUTF8StringEncoding)?.toString()
    }

    @OptIn(BetaInteropApi::class)
    internal fun crypt(
        op: CCOperation,
        data: NSData,
        key: NSData,
        iv: NSData?,
    ): NSData? = memScoped {
        val dataOut = NSMutableData.create(length = (data.length + kCCBlockSizeAES128.toULong())) ?: return null
        val movedBytes = alloc<ULongVar>()

        val status =
            CCCrypt(
                op,
                kCCAlgorithmAES,
                kCCOptionPKCS7Padding,
                key.bytes,
                kCCKeySizeAES256.toULong(),
                iv?.bytes,
                data.bytes,
                data.length,
                dataOut.mutableBytes,
                dataOut.length,
                movedBytes.ptr,
            )

        return if (status == kCCSuccess) {
            dataOut.setLength(movedBytes.value)
            dataOut
        } else {
            null
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun CPointer<*>?.toAny(): Any = this?.let { interpretObjCPointer(it.rawValue) } ?: NSNull()

    private companion object {
        const val V2_PREFIX = "v2:"
        const val IV_LENGTH: ULong = 16u
    }
}
