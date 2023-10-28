import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

fun main() {
    val message = "This is a secret message"
    val key = "SuperSecretKey12SuperSecretKey12".toByteArray(Charsets.UTF_8) // 256-bit key (32 bytes)
	println("Key: $key")
    
    val encryptedMessage = encrypt(message, key)
    println("Encrypted: $encryptedMessage")

    val decryptedMessage = decrypt(encryptedMessage, key)
    println("Decrypted: $decryptedMessage")
}

fun encrypt(plainText: String, key: ByteArray): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKeySpec = SecretKeySpec(key, "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    return Base64.getEncoder().encodeToString(encryptedBytes)
}

fun decrypt(encryptedText: String, key: ByteArray): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKeySpec = SecretKeySpec(key, "AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    val encryptedBytes = Base64.getDecoder().decode(encryptedText)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes, Charsets.UTF_8)
}