package dev.selene.wallet.wallet

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationIntentCreator
import com.solana.mobilewalletadapter.clientlib.scenario.LocalAssociationScenario
import dev.selene.wallet.core.Base58
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

class AuthorizedSession(
    val publicKey: ByteArray,
    val accountLabel: String?,
    val authToken: String?,
    val client: MobileWalletAdapterClient
) {
    val addressBase58: String get() = Base58.encodeToString(publicKey)
}

class MwaWallet(private val context: Context) {

    private var scenario: LocalAssociationScenario? = null

    fun isWalletAvailable(): Boolean {
        return try {
            LocalAssociationIntentCreator.isWalletEndpointAvailable(context.packageManager)
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun connect(openIntent: (Intent) -> Unit): AuthorizedSession {
        val s = LocalAssociationScenario(90_000)
        scenario = s
        val future = s.start()
        val intent = LocalAssociationIntentCreator.createAssociationIntent(null, s.port, s.session)
        openIntent(intent)

        val client: MobileWalletAdapterClient = withContext(Dispatchers.IO) {
            try {
                future.get()
            } catch (e: ExecutionException) {
                throw cause(e)
            }
        }

        val authorization: MobileWalletAdapterClient.AuthorizationResult = withContext(Dispatchers.IO) {
            try {
                client.authorize(
                    Uri.parse("https://selenewallet.app"),
                    null,
                    "Selene Wallet",
                    "solana",
                    null,
                    null,
                    null,
                    null
                ).get()
            } catch (e: ExecutionException) {
                scenario?.close()
                throw cause(e)
            }
        }

        val account = authorization.accounts.firstOrNull()

        return AuthorizedSession(
            publicKey = account?.publicKey ?: authorization.publicKey,
            accountLabel = account?.accountLabel ?: authorization.accountLabel,
            authToken = authorization.authToken,
            client = client
        )
    }

    suspend fun signAndSendTransactions(
        client: MobileWalletAdapterClient,
        transactions: List<ByteArray>
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            client.signAndSendTransactions(transactions.toTypedArray(), null, null, null, null, null)
                .get()
                .signatures
                .map { Base58.encodeToString(it) }
        } catch (e: ExecutionException) {
            throw cause(e)
        }
    }

    fun closeSession() {
        scenario?.close()
        scenario = null
    }

    private fun cause(e: ExecutionException): Exception =
        (e.cause as? Exception) ?: e
}