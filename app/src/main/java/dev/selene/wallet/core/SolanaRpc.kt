package dev.selene.wallet.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class TokenAccount(val pubkey: String, val amount: Long)

data class SignatureInfo(val signature: String, val slot: Long, val error: Boolean)

class SolanaRpc(private val endpoint: String = "https://api.mainnet-beta.solana.com") {

    suspend fun getLatestBlockhash(): String = withContext(Dispatchers.IO) {
        val result = call("getLatestBlockhash", JSONArray().put(JSONObject().put("commitment", "confirmed")))
        result.getJSONObject("value").getString("blockhash")
    }

    suspend fun getBalance(ownerBase58: String): Long = withContext(Dispatchers.IO) {
        call("getBalance", JSONArray().put(ownerBase58).put(JSONObject().put("commitment", "confirmed")))
            .getLong("value")
    }

    suspend fun getTokenAccountsByOwner(ownerBase58: String, mintBase58: String): List<TokenAccount> =
        withContext(Dispatchers.IO) {
            val result = call(
                "getTokenAccountsByOwner",
                JSONArray()
                    .put(ownerBase58)
                    .put(JSONObject().put("mint", mintBase58))
                    .put(JSONObject().put("encoding", "jsonParsed").put("commitment", "confirmed"))
            )
            val array = result.getJSONArray("value")
            val output = ArrayList<TokenAccount>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val pubkey = item.getString("pubkey")
                val info = item.getJSONObject("account").getJSONObject("data").getJSONObject("parsed").getJSONObject("info")
                val amount = info.getJSONObject("tokenAmount").getString("amount").toLongOrNull() ?: 0L
                output.add(TokenAccount(pubkey, amount))
            }
            return@withContext output
        }

    suspend fun getSignaturesForAddress(addressBase58: String, limit: Int = 10): List<SignatureInfo> =
        withContext(Dispatchers.IO) {
            val result = call(
                "getSignaturesForAddress",
                JSONArray()
                    .put(addressBase58)
                    .put(JSONObject().put("limit", limit).put("commitment", "confirmed"))
            )
            val array = result.getJSONArray("value")
            val output = ArrayList<SignatureInfo>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                output.add(
                    SignatureInfo(
                        signature = item.getString("signature"),
                        slot = item.optLong("slot", 0L),
                        error = !item.isNull("err")
                    )
                )
            }
            return@withContext output
        }

    suspend fun getSignatureStatuses(signatures: List<String>): Map<String, Boolean> = withContext(Dispatchers.IO) {
        if (signatures.isEmpty()) return@withContext emptyMap()
        val result = call(
            "getSignatureStatuses",
            JSONArray().put(JSONArray().apply { signatures.forEach { put(it) } })
        )
        val statuses = result.getJSONArray("value")
        val output = HashMap<String, Boolean>()
        for (i in 0 until statuses.length()) {
            val v = statuses.optJSONObject(i)
            if (v != null && !v.isNull("err")) output[signatures[i]] = false
            else output[signatures[i]] = true
        }
        return@withContext output
    }

    private fun call(method: String, params: JSONArray): JSONObject {
        val body = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", System.currentTimeMillis())
            .put("method", method)
            .put("params", params)
            .toString()

        val conn = URL(endpoint).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            val json = JSONObject(text)
            if (json.has("error")) {
                throw RpcException(method, json.getJSONObject("error").optString("message", "$method failed"))
            }
            return json.getJSONObject("result")
        } finally {
            conn.disconnect()
        }
    }
}

class RpcException(method: String, message: String) : Exception("$method: $message")