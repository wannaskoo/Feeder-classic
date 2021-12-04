package com.nononsenseapps.feeder.crypto

import kotlin.test.assertEquals
import org.junit.Test

class AesCbcWithIntegrityTest {
    @Test
    fun generateKeyAndEncryptDecrypt() {
        val originalMessage = "Hello Crypto"

        val key = AesCbcWithIntegrity.generateKey()
        val encryptedMessage = AesCbcWithIntegrity.encryptString(originalMessage, key)
        val decryptedMessage = AesCbcWithIntegrity.decryptString(encryptedMessage, key)

        assertEquals(originalMessage, decryptedMessage)
    }

    @Test
    fun generateKeyAndEncodeDecodeKey() {
        val originalKey = AesCbcWithIntegrity.generateKey()

        val encodedKey = AesCbcWithIntegrity.encodeKey(originalKey)
        val decodedKey = AesCbcWithIntegrity.decodeKey(encodedKey)

        assertEquals(originalKey, decodedKey)
    }
}
