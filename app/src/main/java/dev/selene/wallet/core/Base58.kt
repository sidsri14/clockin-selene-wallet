package dev.selene.wallet.core

object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val INDEXES = IntArray(128) { -1 }.also { arr ->
        ALPHABET.forEachIndexed { i, c -> arr[c.code] = i }
    }

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) zeros++
        val encoded = IntArray((input.size - zeros) * 138 / 100 + 1)
        var length = 0
        var i = zeros
        while (i < input.size) {
            var carry = input[i].toInt() and 0xff
            var j = 0
            while (j < encoded.size) {
                carry += (encoded[j] shl 8) and 0xffff
                encoded[j] = carry % 58
                carry = (carry / 58) and 0xffff
                j++
            }
            i++
        }
        for (k in encoded.lastIndex downTo 0) {
            if (encoded[k] != 0 || length == 0) length++
        }
        val sb = StringBuilder()
        repeat(zeros) { sb.append('1') }
        for (k in length - 1 downTo 0) {
            sb.append(ALPHABET[encoded[k]])
        }
        val result = sb.toString()
        return result
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        var zeros = 0
        while (zeros < input.length && input[zeros] == '1') zeros++
        val b58 = IntArray(input.length - zeros)
        for ((pos, c) in input.withIndex().drop(zeros)) {
            val value = if (c.code < INDEXES.size) INDEXES[c.code] else -1
            require(value != -1) { "Invalid base58 character '$c'" }
            carry(value, b58)
        }
        var high = b58.size - 1
        while (high > 0 && b58[high] == 0) high--
        val bytes = ByteArray(zeros + high + 1)
        var pos = 0
        val out = IntArray(bytes.size)
        for (k in high downTo 0) {
            var carry = b58[k]
            var j = 0
            while (j < pos + 1 || carry != 0) {
                val x = (out[j] and 0xff) + carry * 58
                out[j] = (x and 0xff) shl 24 shr 24
                carry = x ushr 8
                j++
            }
            pos = j - 1
        }
        System.arraycopy(out, 0, bytes, zeros, high + 1)
        return bytes
    }

    private fun carry(value: Int, b58: IntArray) {
        var carry = value
        var j = 0
        while (carry != 0 || j < b58.size) {
            var t = carry + b58[j] * 58
            b58[j] = t and 0xff
            carry = (t ushr 8) and 0xffff
            j++
        }
    }

    fun decodeToByteArray(input: String): ByteArray = decode(input)
    fun encodeToString(input: ByteArray): String = encode(input)
}