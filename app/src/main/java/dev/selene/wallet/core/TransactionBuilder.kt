package dev.selene.wallet.core

class AccountMeta(val pubkey: ByteArray, val isSigner: Boolean, val isWritable: Boolean)

class CompiledInstruction(
    val programId: ByteArray,
    val accountKeys: List<ByteArray>,
    val data: ByteArray
)

class LegacyMessage(
    private val metas: List<AccountMeta>,
    val recentBlockhash: ByteArray,
    private val instructions: List<CompiledInstruction>
) {
    fun compile(): ByteArray {
        val order = LinkedHashMap<String, ByteArray>()
        val signer = HashMap<String, Boolean>()
        val writable = HashMap<String, Boolean>()
        for (m in metas) {
            val k = Base58.encodeToString(m.pubkey)
            if (!order.containsKey(k)) {
                order[k] = m.pubkey
                signer[k] = m.isSigner
                writable[k] = m.isWritable
            } else {
                if (m.isSigner) signer[k] = true
                if (m.isWritable) writable[k] = true
            }
        }

        val writableSigned = mutableListOf<ByteArray>()
        val readonlySigned = mutableListOf<ByteArray>()
        val writableUnsigned = mutableListOf<ByteArray>()
        val readonlyUnsigned = mutableListOf<ByteArray>()
        for (key in order.values) {
            val k = Base58.encodeToString(key)
            val isSigner = signer[k] ?: false
            val isWritable = writable[k] ?: false
            when {
                isSigner && isWritable -> writableSigned.add(key)
                isSigner -> readonlySigned.add(key)
                isWritable -> writableUnsigned.add(key)
                else -> readonlyUnsigned.add(key)
            }
        }
        // web3 sorts the 4 buckets by (signer desc, writable desc), then base58 string
        // ascending inside each bucket (matches @solana/web3.js compileMessage sort).
        fun sorted(bucket: MutableList<ByteArray>): List<ByteArray> =
            bucket.sortedBy { Base58.encodeToString(it) }
        val accountKeys = sorted(writableSigned) + sorted(readonlySigned) +
            sorted(writableUnsigned) + sorted(readonlyUnsigned)
        val indexOf = accountKeys.map { Base58.encodeToString(it) }

        val numRequiredSignatures = writableSigned.size + readonlySigned.size
        val numReadonlySignedAccounts = readonlySigned.size
        val numReadonlyUnsignedAccounts = readonlyUnsigned.size

        val out = ByteArrayOutputStreamLE()
        out.writeByte(numRequiredSignatures)
        out.writeByte(numReadonlySignedAccounts)
        out.writeByte(numReadonlyUnsignedAccounts)
        out.writeShortVec(accountKeys.size)
        accountKeys.forEach { out.write(it) }
        out.write(recentBlockhash)
        out.writeShortVec(instructions.size)
        for (ins in instructions) {
            out.writeByte(indexOf.indexOf(Base58.encodeToString(ins.programId)))
            out.writeShortVec(ins.accountKeys.size)
            for (acc in ins.accountKeys) {
                out.writeByte(indexOf.indexOf(Base58.encodeToString(acc)))
            }
            out.writeShortVec(ins.data.size)
            out.write(ins.data)
        }
        return out.toByteArray()
    }
}

class ByteArrayOutputStreamLE {
    private val buffer = ArrayList<Byte>()

    fun writeByte(value: Int) {
        buffer.add((value and 0xff).toByte())
    }

    fun write(bytes: ByteArray) {
        bytes.forEach { buffer.add(it) }
    }

    fun writeShortVec(value: Int) {
        var v = value
        while (true) {
            val byte = (v and 0x7f).toByte()
            v = v ushr 7
            if (v != 0) {
                buffer.add((byte.toInt() or 0x80).toByte())
            } else {
                buffer.add(byte)
                return
            }
        }
    }

    fun toByteArray(): ByteArray = buffer.toByteArray()
}

fun u64Le(value: Long): ByteArray {
    val out = ByteArray(8)
    var v = value
    for (i in 0 until 8) {
        out[i] = (v and 0xff).toByte()
        v = v ushr 8
    }
    return out
}

/**
 * Serializes an unsigned legacy transaction: compact-u16 signature count followed by a
 * 64-byte padding signature per signer, then the message. Required by Mobile Wallet
 * Adapter, which validates the transaction envelope before requesting signatures.
 */
fun wrapUnsignedTransaction(message: ByteArray, numSigners: Int = 1): ByteArray {
    val out = ByteArrayOutputStreamLE()
    out.writeShortVec(numSigners)
    repeat(numSigners) { out.write(ByteArray(64)) }
    out.write(message)
    return out.toByteArray()
}

object SystemProgram {
    const val ID = "11111111111111111111111111111111"
    const val TRANSFER = 2
}

object TokenProgram {
    const val ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TRANSFER = 3
}

object AssociatedTokenProgram {
    const val ID = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL"
}

object TransactionBuilder {
    fun solTransfer(from: ByteArray, to: ByteArray, lamports: Long, recentBlockhash: ByteArray): ByteArray {
        val system = Base58.decodeToByteArray(SystemProgram.ID)
        val metas = listOf(
            AccountMeta(from, true, true),
            AccountMeta(to, false, true),
            AccountMeta(system, false, false)
        )
        // System program encodes its instruction tag as u32 LE (matches @solana/web3.js).
        val data = ByteArrayOutputStreamLE().apply {
            writeByte(SystemProgram.TRANSFER)
            writeByte(0)
            writeByte(0)
            writeByte(0)
            write(u64Le(lamports))
        }.toByteArray()
        val instruction = CompiledInstruction(
            programId = system,
            accountKeys = listOf(from, to),
            data = data
        )
        return LegacyMessage(metas, recentBlockhash, listOf(instruction)).compile()
    }

    fun splTransfer(
        fromOwner: ByteArray,
        fromTokenAccount: ByteArray,
        toTokenAccount: ByteArray,
        amount: Long,
        recentBlockhash: ByteArray
    ): ByteArray {
        val token = Base58.decodeToByteArray(TokenProgram.ID)
        val metas = listOf(
            AccountMeta(fromOwner, true, true),
            AccountMeta(fromTokenAccount, false, true),
            AccountMeta(toTokenAccount, false, true),
            AccountMeta(token, false, false)
        )
        val data = ByteArrayOutputStreamLE().apply {
            writeByte(TokenProgram.TRANSFER)
            write(u64Le(amount))
        }.toByteArray()
        val instruction = CompiledInstruction(
            programId = token,
            accountKeys = listOf(fromTokenAccount, toTokenAccount, fromOwner),
            data = data
        )
        return LegacyMessage(metas, recentBlockhash, listOf(instruction)).compile()
    }

    fun createAssociatedTokenAccount(
        funder: ByteArray,
        ata: ByteArray,
        owner: ByteArray,
        mint: ByteArray
    ): CompiledInstruction {
        // spl-token 0.4.x emits an empty data payload for the classic ATA create
        // instruction (matches @solana/web3.js createAssociatedTokenAccountInstruction).
        val data = ByteArray(0)
        return CompiledInstruction(
            programId = Base58.decodeToByteArray(AssociatedTokenProgram.ID),
            accountKeys = listOf(
                funder,
                ata,
                owner,
                mint,
                Base58.decodeToByteArray(SystemProgram.ID),
                Base58.decodeToByteArray(TokenProgram.ID)
            ),
            data = data
        )
    }

    fun splTransferCreate(
        feePayer: ByteArray,
        fromTokenAccount: ByteArray,
        toOwner: ByteArray,
        mint: ByteArray,
        amount: Long,
        recentBlockhash: ByteArray
    ): ByteArray {
        val ata = AssociatedToken.deriveAta(mint, toOwner)
        val atoken = Base58.decodeToByteArray(AssociatedTokenProgram.ID)
        val token = Base58.decodeToByteArray(TokenProgram.ID)
        val system = Base58.decodeToByteArray(SystemProgram.ID)

        val create = CompiledInstruction(
            programId = atoken,
            accountKeys = listOf(feePayer, ata, toOwner, mint, system, token),
            data = ByteArray(0)
        )
        val transfer = CompiledInstruction(
            programId = token,
            accountKeys = listOf(fromTokenAccount, ata, feePayer),
            data = ByteArrayOutputStreamLE().apply {
                writeByte(TokenProgram.TRANSFER)
                write(u64Le(amount))
            }.toByteArray()
        )

        val metas = listOf(
            AccountMeta(feePayer, true, true),
            AccountMeta(fromTokenAccount, false, true),
            AccountMeta(ata, false, true),
            AccountMeta(toOwner, false, false),
            AccountMeta(mint, false, false),
            AccountMeta(system, false, false),
            AccountMeta(token, false, false),
            AccountMeta(atoken, false, false)
        )
        return LegacyMessage(metas, recentBlockhash, listOf(create, transfer)).compile()
    }
}