package com.application.androcom

// dependencies
import android.os.Build
import androidx.annotation.RequiresApi
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

// encryption method: AES (Advanced Encryption Standard)
class AESEncryption {

    // function to encrypt text
    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(text: String, secretKey: String): String {

        // generate key from a secret key
        val key = generateKey(secretKey)

        // get Aes method
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

        // set mode
        cipher.init(Cipher.ENCRYPT_MODE, key)

        // encrypt
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))

        // convert back to string
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // function to decrypt
    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(encryptedText: String, secretKey: String): String {

        // generate key
        val key = generateKey(secretKey)

        // get AES method
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

        // set mode
        cipher.init(Cipher.DECRYPT_MODE, key)

        // convert encrypted text to byte code
        val decodedBytes = Base64.getDecoder().decode(encryptedText)

        // decrypt
        val decryptedBytes = cipher.doFinal(decodedBytes)

        // convert to string
        return String(decryptedBytes, Charsets.UTF_8)
    }

    // function to generate a key from secret key
    private fun generateKey(secretKey: String): SecretKeySpec {

        // create a 16-Byte array
        val keyData = ByteArray(16)

        // convert secret key to byte array
        val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)

        for (i in secretKeyBytes.indices) {
            keyData[i % 16] = (keyData[i % 16] + secretKeyBytes[i]).toByte()
        }
        // this might generate different each time

        // SecretkeySpec is just a defined datatype for keys
        // this is generated same each time
        return SecretKeySpec(keyData, "AES")
    }
}
