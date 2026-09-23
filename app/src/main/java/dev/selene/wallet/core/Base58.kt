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
        var buf = input.copyOfRange(zeros, input.size)
        val digits = ArrayList<Int>()
        while (buf.isNotEmpty()) {
            var remainder = 0
            for (i in buf.indices) {
                val cur = (remainder shl 8) or (buf[i].toInt() and 0xff)
                buf[i] = (cur / 58).toByte()
                remainder = cur % 58
            }
            digits.add(remainder)
            var lead = 0
            while (lead < buf.size && buf[lead].toInt() == 0) lead++
            buf = if (lead >= buf.size) ByteArray(0) else buf.copyOfRange(lead, buf.size)
        }
        val sb = StringBuilder()
        repeat(zeros) { sb.append('1') }
        for (i in digits.indices.reversed()) {
            sb.append(ALPHABET[digits[i]])
        }
        return sb.toString()
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        var zeros = 0
        while (zeros < input.length && input[zeros] == '1') zeros++
        val b58 = IntArray(input.length - zeros)
        for ((pos, c) in input.withIndex().drop(zeros)) {
            val value = if (c.code < INDEXES.size) INDEXES[c.code] else -1
            require(value != -1) { "Invalid base58 character '$c'" }
            var carry = value
            var j = 0
            while (carry != 0 || j < b58.size) {
                val t = carry + b58[j] * 58
                b58[j] = t and 0xff
                carry = (t ushr 8) and 0xffff
                j++
            }
        }
        var high = b58.size - 1
        while (high > 0 && b58[high] == 0) high--
        val bytes = ByteArray(zeros + high + 1)
        for (k in 0..high) {
            bytes[zeros + (high - k)] = b58[k].toByte()
        }
        return bytes
    }

    fun decodeToByteArray(input: String): ByteArray = decode(input)
    fun encodeToString(input: ByteArray): String = encode(input)
}