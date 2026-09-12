package dev.selene.wallet.core

class AccountMeta(val pubkey: ByteArray, val isSigner: Boolean, val isWritable: Boolean)

class CompiledInstruction(
    val programIdIndex: Int,
    val accountIndexes: IntArray,
    val data: ByteArray
)

class LegacyMessage(
    metas: List<AccountMeta>,
    val recentBlockhash: ByteArray,
    val instructions: List<CompiledInstruction>
) {
    val numRequiredSignatures = metas.count { it.isSigner }
    val numReadonlySignedAccounts = metas.count { it.isSigner && !it.isWritable }
    val numReadonlyUnsignedAccounts = metas.count { !it.isSigner && !it.isWritable }
    val accountKeys = dedupe(metas)

    private fun dedupe(metas: List<AccountMeta>): List<ByteArray> {
        val seen = LinkedHashMap<String, ByteArray>()
        for (m in metas) {
            val key = Base58.encodeToString(m.pubkey)
            if (!seen.containsKey(key)) seen[key] = m.pubkey
        }
        return seen.values.toList()
    }

    fun compile(): ByteArray {
        val out = ByteArrayOutputStreamLE()
        out.writeByte(numRequiredSignatures)
        out.writeByte(numReadonlySignedAccounts)
        out.writeByte(numReadonlyUnsignedAccounts)
        out.writeShortVec(accountKeys.size)
        accountKeys.forEach { out.write(it) }
        out.write(recentBlockhash)
        out.writeShortVec(instructions.size)
        for (ins in instructions) {
            out.writeByte(ins.programIdIndex)
            out.writeShortVec(ins.accountIndexes.size)
            ins.accountIndexes.forEach { out.writeByte(it) }
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

object SystemProgram {
    const val ID = "11111111111111111111111111111111"
    const val TRANSFER = 2
}

object TokenProgram {
    const val ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TRANSFER = 3
}

object TransactionBuilder {
    fun solTransfer(from: ByteArray, to: ByteArray, lamports: Long, recentBlockhash: ByteArray): ByteArray {
        val system = Base58.decodeToByteArray(SystemProgram.ID)
        val metas = listOf(
            AccountMeta(from, true, true),
            AccountMeta(to, false, true),
            AccountMeta(system, false, false)
        )
        val keyIndex = metas.map { Base58.encodeToString(it.pubkey) }
        val data = ByteArrayOutputStreamLE().apply {
            writeByte(SystemProgram.TRANSFER)
            write(u64Le(lamports))
        }.toByteArray()
        val instruction = CompiledInstruction(
            programIdIndex = keyIndex.indexOf(Base58.encodeToString(system)),
            accountIndexes = intArrayOf(keyIndex.indexOf(Base58.encodeToString(to))),
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
        val keyIndex = metas.map { Base58.encodeToString(it.pubkey) }
        val data = ByteArrayOutputStreamLE().apply {
            writeByte(TokenProgram.TRANSFER)
            write(u64Le(amount))
        }.toByteArray()
        val instruction = CompiledInstruction(
            programIdIndex = keyIndex.indexOf(Base58.encodeToString(token)),
            accountIndexes = intArrayOf(
                keyIndex.indexOf(Base58.encodeToString(fromTokenAccount)),
                keyIndex.indexOf(Base58.encodeToString(toTokenAccount)),
                keyIndex.indexOf(Base58.encodeToString(fromOwner))
            ),
            data = data
        )
        return LegacyMessage(metas, recentBlockhash, listOf(instruction)).compile()
    }
}