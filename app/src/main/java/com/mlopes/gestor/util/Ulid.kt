package com.mlopes.gestor.util

import java.util.concurrent.atomic.AtomicLong

/**
 * Gerador de ULID (26 chars, ordenavel por tempo).
 * Mesmo formato do Gestor desktop (src/js/backend/ulid.js) e do plugin WP.
 *
 * Formato: 10 chars timestamp (base32 Crockford) + 16 chars random (base32 Crockford).
 * ALPHABET identico ao desktop pra interoperabilidade.
 */
object Ulid {
    private const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val TIME_LEN = 10
    private const val RAND_LEN = 16
    private val lastTime = AtomicLong(0L)
    private var lastRandom = ""

    @Synchronized
    fun next(): String {
        val now = System.currentTimeMillis()
        val time = if (now == lastTime.get()) lastTime.get() else now
        if (now == lastTime.get() && lastRandom.isNotEmpty()) {
            lastRandom = incrementRandom(lastRandom)
        } else {
            lastRandom = randomChars(RAND_LEN)
        }
        lastTime.set(time)
        return encodeTime(time) + lastRandom
    }

    private fun encodeTime(ms: Long): String {
        var v = ms
        val sb = StringBuilder(TIME_LEN)
        for (i in TIME_LEN - 1 downTo 0) {
            sb.append(ALPHABET[(v % 32).toInt()])
            v = v / 32
        }
        return sb.toString()
    }

    private fun randomChars(len: Int): String {
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            sb.append(ALPHABET[(Math.random() * 32).toInt()])
        }
        return sb.toString()
    }

    private fun incrementRandom(r: String): String {
        val chars = r.toCharArray()
        for (i in chars.size - 1 downTo 0) {
            val idx = ALPHABET.indexOf(chars[i])
            if (idx < 31) {
                chars[i] = ALPHABET[idx + 1]
                return String(chars)
            }
            chars[i] = '0'
        }
        return "0" + String(chars)
    }
}
