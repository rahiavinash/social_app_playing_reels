package com.wedo8020.app.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log
import com.wedo8020.app.BuildConfig
import com.wedo8020.app.utils.Utils.toJsonString

object CryptoManager {
    private val secretKey = BuildConfig.ENCRYPT_SECRET
    private val iv = BuildConfig.ENCRYPT_SECRET

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        val ivSpec = IvParameterSpec(iv.substring(0,16).toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decrypt(ciphertext: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(iv.substring(0,16).toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedCiphertext = Base64.decode(ciphertext, Base64.NO_WRAP)
            val decrypted = cipher.doFinal(decodedCiphertext)
            String(decrypted)
        }catch (e: Exception){
            Log.e("sdfgdfd",e.toString())
            val fakeRes = FakeResponse()
            fakeRes.toJsonString()
        }

    }


}

data class FakeResponse(
    val status: Boolean = false,
    val message: String = "Something went wrong"
)