package dev.selene.wallet.core

import java.math.BigInteger

/**
 * Minimal ed25519 field arithmetic: enough to check whether a 32-byte encoded point
 * lies on the curve. Off-curve addresses are required for Solana program-derived
 * addresses (PDA), so [isOnCurve] must match ed25519-dalek's compress/decompress.
 */
object Ed25519 {
    private val P = BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819949")
    private val D = BigInteger("37095705934669439343138083508754565189542113879843219016388785533085940283555")
    private val ONE = BigInteger.ONE
    private val TWO = BigInteger.valueOf(2)

    /** Returns true when [encoded] is a valid ed25519 compressed point. */
    fun isOnCurve(encoded: ByteArray): Boolean {
        if (encoded.size != 32) return false
        val yBytes = encoded.copyOf()
        yBytes[31] = (yBytes[31].toInt() and 0x7f).toByte()

        var y = BigInteger.ZERO
        for (i in yBytes.indices) {
            y = y.or(BigInteger.valueOf((yBytes[i].toInt() and 0xff).toLong()).shiftLeft(8 * i))
        }
        if (y.compareTo(P) >= 0) return false

        val y2 = y.modPow(TWO, P)
        val num = y2.subtract(ONE).mod(P)
        val den = y2.multiply(D).add(ONE).mod(P)
        if (den.signum() == 0) return false

        // A 32-byte encoding is a valid point iff x^2 = (y^2-1)/(d*y^2+1) is a
        // quadratic residue (dalek: CompressedEdwardsY + sqrt_ratio_i). The
        // sign/parity bit is resolved during decompression, not membership.
        val x2 = num.multiply(den.modInverse(P)).mod(P)
        return sqrtModP(x2) != null
    }

    /** Tonelli–Shanks square root in GF(P). Returns null when [a] is not a quadratic residue. */
    private fun sqrtModP(a: BigInteger): BigInteger? {
        if (a.signum() == 0) return BigInteger.ZERO

        var q = P.subtract(ONE)
        var s = 0
        while (!q.testBit(0)) {
            s++
            q = q.shiftRight(1)
        }
        if (s == 1) {
            val r = a.modPow(q.add(ONE).shiftRight(1), P)
            return if (r.modPow(TWO, P) == a) r else null
        }

        var z = TWO
        while (z.modPow(P.subtract(ONE).shiftRight(1), P) == ONE) {
            z = z.add(ONE)
        }
        var c = z.modPow(q, P)
        var x = a.modPow(q.add(ONE).shiftRight(1), P)
        var t = a.modPow(q, P)
        var m = s
        while (t != ONE) {
            var i = 1
            var tt = t.multiply(t).mod(P)
            while (tt != ONE) {
                tt = tt.multiply(tt).mod(P)
                i++
            }
            if (i == m) return null
            val b = c.modPow(ONE.shiftLeft(m - i - 1), P)
            x = x.multiply(b).mod(P)
            c = b.multiply(b).mod(P)
            t = t.multiply(c).mod(P)
            m = i
        }
        return x
    }
}