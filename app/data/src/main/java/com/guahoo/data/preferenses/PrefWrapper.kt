package com.guahoo.data.preferenses

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

@Suppress("UNCHECKED_CAST")
class PrefWrapper<T>(
    private val sharedPrefs: SharedPreferences,
    private val key: String,
    private val secured: Boolean = true
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return when (property.returnType) {
            typeOf<String?>() -> {
                if (secured) {
                    val resultString = sharedPrefs.getString(key, null)
                    if (resultString.isNullOrEmpty()) {
                        null
                    } else {
                        decodeData(resultString) as T?
                    }
                } else {
                    sharedPrefs.getString(key, null) as T?
                }
            }
            typeOf<Boolean?>() -> {
                sharedPrefs.getBoolean(key, false) as T
            }
            typeOf<Long?>() -> {
                if(!sharedPrefs.contains(key)) null
                else sharedPrefs.getLong(key, 0) as T?
            }
            typeOf<Int?>() -> {
                if(!sharedPrefs.contains(key)) null
                else sharedPrefs.getInt(key, 0) as T?
            }
            typeOf<LocalDate?>() -> {
                if (!sharedPrefs.contains(key)) {
                    null
                } else {
                    try {
                        val dateString = sharedPrefs.getString(key, null)
                        LocalDate.parse(dateString) as T?
                    } catch (ex: DateTimeParseException) {
                    //    L.d("parse exception: $ex")
                        null
                    }

                }
            }
            else -> throw IllegalArgumentException("UnsupportedType")
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value == null) {
            sharedPrefs.edit().apply {
                remove(key)
                apply()
            }
            return
        }

        sharedPrefs.edit().apply {
            when (property.returnType) {
                typeOf<String?>() -> {
                    putString(key, if (secured) encodeData(value as String) else value as String)
                }
                typeOf<Boolean?>() -> {
                    putBoolean(key, value as Boolean)
                }
                typeOf<Long?>() -> {
                    putLong(key, value as Long)
                }
                typeOf<Int?>() -> {
                    putInt(key, value as Int)
                }
                typeOf<LocalDate?>() -> {
                    try {
                        val dateString = (value as LocalDate).format(DateTimeFormatter.ISO_LOCAL_DATE)
                        putString(key, dateString)
                    } catch (ex: DateTimeParseException) {
                     //   L.d("format exception: $ex")
                    }
                }
                else -> throw IllegalArgumentException("UnsupportedType")
            }
            apply()
        }
    }

    companion object {
        private const val TRANSFORMATION_ALGORITHM = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val ALIAS_VALUE = "alias value for key store"

        fun encodeData(inputString: String): String {
            if (inputString.isEmpty()) {
                return inputString
            }

            var data: String = inputString

            if (data.length % 4 != 0) {
                var zeroString = ""
                for(i in 1..4 - data.length % 4)
                    zeroString += "0"
                data = zeroString + inputString
            }

            var encodedBytes = ""
            try {
                val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
                encodedBytes = Base64.encodeToString(cipher.iv, Base64.DEFAULT) + IV_SEPARATOR
                val dataBytes = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))
                encodedBytes += Base64.encodeToString(dataBytes, Base64.DEFAULT)
            } catch (e: java.lang.Exception) {
                //L.e(e.localizedMessage)
            }

         //   L.d("encodedBytes + iv $encodedBytes")
            return encodedBytes
        }

        fun decodeData(encodedString: String): String {
            var decodedString = ""
            val split = encodedString.split(IV_SEPARATOR.toRegex())

            if (split.getOrNull(1).isNullOrEmpty()) {
                return "0"
            }

            try {
                val cipher = Cipher.getInstance(TRANSFORMATION_ALGORITHM)
                val spec = GCMParameterSpec(128, Base64.decode(split[0], Base64.DEFAULT))

                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
                val decodedBytes = cipher.doFinal(Base64.decode(split[1], Base64.DEFAULT))
                decodedString = Base64.encodeToString(decodedBytes, Base64.DEFAULT)
            } catch (e: java.lang.Exception) {
             //   L.e(e.localizedMessage)
            }

            while (decodedString.isNotEmpty() && decodedString[0] == '0') {
                decodedString = decodedString.replaceFirst("0", "")
            }

           // L.d("decoded string $decodedString")
            return decodedString
        }

        @SuppressLint("NewApi")
        fun getSecretKey(): SecretKey? {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)

            if (keyStore.containsAlias(ALIAS_VALUE)) {
                val secretKeyEntry = keyStore.getEntry(ALIAS_VALUE, null) as KeyStore.SecretKeyEntry
                return secretKeyEntry.secretKey
            } else {
                try {
                    val generator: KeyGenerator =
                        KeyGenerator.getInstance(
                            KeyProperties.KEY_ALGORITHM_AES,
                            ANDROID_KEY_STORE
                        )
                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        ALIAS_VALUE,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()

                    generator.init(keyGenParameterSpec)
                    return generator.generateKey()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return null
        }
    }
}