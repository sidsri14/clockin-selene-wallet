package dev.selene.wallet.core

import java.security.MessageDigest

/**
 * Solana program-derived address (PDA) derivation and the Associated Token Account
 * derivation used by the SPL ATA program:
 *
 *   ATA = findProgramAddress([owner, mint, tokenProgramId], ATokenProgramId)
 *
 * The bump starts at 255 and decreases until the SHA-256 hash of the seeds lands on
 * an off-curve ed25519 point (this matches @solana/web3.js's nonce loop and the
 * spl-associated-token-account on-chain derivation).
 */
object AssociatedToken {

    fun deriveAta(mint: ByteArray, owner: ByteArray): ByteArray {
        val tokenProgram = Base58.decodeToByteArray(TokenProgram.ID)
        val atokenProgram = Base58.decodeToByteArray(AssociatedTokenProgram.ID)
        return findProgramAddress(listOf(owner, tokenProgram, mint), atokenProgram).first
    }

    fun findProgramAddress(seeds: List<ByteArray>, programId: ByteArray): Pair<ByteArray, Byte> {
        val sha256 = MessageDigest.getInstance("SHA-256")
        // Hash domain: seeds || bump || programId || "ProgramDerivedAddress"
        val pdaSuffix = "ProgramDerivedAddress".toByteArray()
        for (bump in 255 downTo 0) {
            sha256.reset()
            for (seed in seeds) sha256.update(seed)
            sha256.update(bump.toByte())
            sha256.update(programId)
            sha256.update(pdaSuffix)
            val hash = sha256.digest()
            if (Ed25519.isOnCurve(hash)) continue
            return hash to bump.toByte()
        }
        throw IllegalArgumentException("Unable to find a viable program address bump seed")
    }
}