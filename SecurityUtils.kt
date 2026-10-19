package com.vampire.browser.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    private const val ITERATIONS = 12000
    private const val KEY_LENGTH = 256

    fun hashPin(pin: String): Pair<String, String> {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded

        val saltStr = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashStr = Base64.encodeToString(hash, Base64.NO_WRAP)
        return Pair(hashStr, saltStr)
    }

    fun verifyPin(pin: String, storedHash: String, storedSalt: String): Boolean {
        return try {
            val salt = Base64.decode(storedSalt, Base64.NO_WRAP)
            val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val testHash = factory.generateSecret(spec).encoded
            val testHashStr = Base64.encodeToString(testHash, Base64.NO_WRAP)
            testHashStr == storedHash
        } catch (e: Exception) {
            false
        }
    }
}