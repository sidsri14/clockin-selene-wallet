package dev.selene.wallet.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions as KeyboardOptionsAlias
import dev.selene.wallet.core.Base58
import dev.selene.wallet.core.SignatureInfo
import dev.selene.wallet.core.SolanaRpc
import dev.selene.wallet.core.TransactionBuilder
import dev.selene.wallet.wallet.MwaWallet
import kotlinx.coroutines.launch
import java.math.BigDecimal

const val USDC_MINT = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
const val LAMPORTS_PER_SOL = 1_000_000_000L
const val USDC_DECIMALS = 1_000_000L

enum class Asset(val label: String) {
    SOL("SOL"),
    USDC("USDC")
}

sealed interface AppState {
    object Idle : AppState
    object Connecting : AppState
    data class NeedsWallet(val message: String) : AppState
    data class Connected(val session: dev.selene.wallet.wallet.AuthorizedSession) : AppState
    data class Failed(val message: String) : AppState
}

@Composable
fun SeleneApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mwa = remember { MwaWallet(context) }
    val rpc = remember { SolanaRpc() }
    val clipboard = LocalClipboardManager.current

    var appState by remember { mutableStateOf<AppState>(AppState.Idle) }
    var solLamports by remember { mutableLongStateOf(0L) }
    var usdcUnits by remember { mutableLongStateOf(0L) }
    var history by remember { mutableStateOf<List<SignatureInfo>>(emptyList()) }
    var historyStatuses by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var refreshing by remember { mutableStateOf(false) }
    var showSend by remember { mutableStateOf(false) }
    var sendError by remember { mutableStateOf<String?>(null) }

    val connected = (appState as? AppState.Connected)?.session
    val address = connected?.addressBase58.orEmpty()

    fun refreshHistory(refreshBalances: Boolean = true) {
        val sess = connected ?: return
        refreshing = true
        scope.launch {
            try {
                if (refreshBalances) {
                    solLamports = rpc.getBalance(sess.addressBase58)
                    usdcUnits = rpc.getTokenAccountsByOwner(sess.addressBase58, USDC_MINT).sumOf { it.amount }
                }
                val sigs = rpc.getSignaturesForAddress(sess.addressBase58, 12)
                history = sigs
                historyStatuses = rpc.getSignatureStatuses(sigs.map { it.signature })
            } catch (_: Exception) {
            } finally {
                refreshing = false
            }
        }
    }

    fun connect() {
        if (!mwa.isWalletAvailable()) {
            appState = AppState.NeedsWallet(
                "No Mobile Wallet Adapter app is installed.\n\nInstall Phantom or Solflare from the Solana dApp Store, or use a Solana smart phone (Seeker) via the MWA ecosystem."
            )
            return
        }
        appState = AppState.Connecting
        scope.launch {
            try {
                val session = mwa.connect { intent: Intent -> context.startActivity(intent) }
                appState = AppState.Connected(session)
                refreshHistory()
            } catch (e: Exception) {
                mwa.closeSession()
                appState = AppState.Failed(e.message ?: "Unable to connect to wallet")
            }
        }
    }

    fun disconnect() {
        mwa.closeSession()
        appState = AppState.Idle
        solLamports = 0L
        usdcUnits = 0L
        history = emptyList()
        historyStatuses = emptyMap()
    }

    fun send(asset: Asset, recipientText: String, amountText: String) {
        val sess = connected ?: return
        sendError = null
        scope.launch {
            try {
                val recipient = parseAddress(recipientText)
                    ?: throw IllegalArgumentException("Invalid recipient address")
                val blockhashBytes = Base58.decodeToByteArray(rpc.getLatestBlockhash())
                val lamports: Long
                val message: ByteArray
                when (asset) {
                    Asset.SOL -> {
                        lamports = positiveLamports(amountText)
                        message = TransactionBuilder.solTransfer(sess.publicKey, recipient, lamports, blockhashBytes)
                    }
                    Asset.USDC -> {
                        val units = positiveUsdc(amountText)
                        val from = rpc.getTokenAccountsByOwner(sess.addressBase58, USDC_MINT)
                            .firstOrNull { it.amount >= units }
                            ?: throw IllegalArgumentException("Insufficient USDC balance")
                        val existingDest = rpc.getTokenAccountsByOwner(Base58.encodeToString(recipient), USDC_MINT)
                            .firstOrNull()
                        message = if (existingDest == null) {
                            TransactionBuilder.splTransferCreate(
                                feePayer = sess.publicKey,
                                fromTokenAccount = Base58.decodeToByteArray(from.pubkey),
                                toOwner = recipient,
                                mint = Base58.decodeToByteArray(USDC_MINT),
                                amount = units,
                                recentBlockhash = blockhashBytes
                            )
                        } else {
                            TransactionBuilder.splTransfer(
                                sess.publicKey,
                                Base58.decodeToByteArray(from.pubkey),
                                Base58.decodeToByteArray(existingDest.pubkey),
                                units,
                                blockhashBytes
                            )
                        }
                    }
                }
                val signatures = mwa.signAndSendTransactions(sess.client, listOf(message))
                Toast.makeText(context, "Sent! ${signatures.first().take(16)}…", Toast.LENGTH_LONG).show()
                showSend = false
                refreshHistory()
            } catch (e: Exception) {
                sendError = e.message ?: "Send failed"
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SeleneNight) {
        when (val state = appState) {
            is AppState.Idle ->
                Landing(message = null, onConnect = ::connect)
            is AppState.NeedsWallet ->
                Landing(message = state.message, onConnect = ::connect)
            is AppState.Failed ->
                Landing(message = state.message, onConnect = ::connect)
            is AppState.Connecting ->
                ConnectingScreen()
            is AppState.Connected ->
                Dashboard(
                    address = address,
                    solLamports = solLamports,
                    usdcUnits = usdcUnits,
                    history = history,
                    historyStatuses = historyStatuses,
                    refreshing = refreshing,
                    onRefresh = { refreshHistory() },
                    onSend = { showSend = true },
                    onDisconnect = ::disconnect,
                    onCopy = { text ->
                        clipboard.setText(AnnotatedString(text))
                        Toast.makeText(context, "Address copied", Toast.LENGTH_SHORT).show()
                    }
                )
        }
    }

    if (showSend && connected != null) {
        SendDialog(
            onDismiss = { showSend = false },
            onSend = { asset, recipient, amount -> send(asset, recipient, amount) },
            error = sendError
        )
    }
}

@Composable
private fun Landing(message: String?, onConnect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SeleneNight, Color(0xFF171033), SeleneNight)
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SeleneAmethyst, SeleneMint))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.LightMode,
                contentDescription = null,
                tint = SeleneNight,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Selene", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "A private, self-custody wallet for the Solana ecosystem",
            textAlign = TextAlign.Center,
            color = SeleneTextDim,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onConnect,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SeleneAmethyst)
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = SeleneNight)
            Spacer(Modifier.width(8.dp))
            Text("Connect wallet", fontWeight = FontWeight.Bold, color = SeleneNight)
        }
        if (message != null) {
            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SeleneSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text(message, color = SeleneText, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ConnectingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SeleneNight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = SeleneAmethyst)
        Spacer(Modifier.height(24.dp))
        Text("Opening wallet…", color = SeleneTextDim)
        Text(
            "Approve the connection in your mobile wallet",
            style = MaterialTheme.typography.bodySmall,
            color = SeleneTextDim.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun Dashboard(
    address: String,
    solLamports: Long,
    usdcUnits: Long,
    history: List<SignatureInfo>,
    historyStatuses: Map<String, Boolean>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onSend: () -> Unit,
    onDisconnect: () -> Unit,
    onCopy: (String) -> Unit
) {
    val totalSol = solLamports / LAMPORTS_PER_SOL.toDouble()
    val totalUsdc = usdcUnits / USDC_DECIMALS.toDouble()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SeleneNight, Color(0xFF171033), SeleneNight)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(SeleneAmethyst, SeleneMint))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LightMode, contentDescription = null, tint = SeleneNight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Selene", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = address.take(6) + "…" + address.takeLast(6),
                                    fontFamily = FontFamily.Monospace,
                                    color = SeleneTextDim,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy address",
                                    tint = SeleneTextDim,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onCopy(address) }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Filled.Close, contentDescription = "Disconnect", tint = SeleneTextDim)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2A1E5C), Color(0xFF123B3A))),
                            RoundedCornerShape(20.dp)
                        )
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Total balance", color = SeleneTextDim, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "%.4f SOL".format(totalSol),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("+  %.2f USDC".format(totalUsdc), color = SeleneMint, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onSend,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SeleneAmethyst)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = SeleneNight)
                            Spacer(Modifier.width(8.dp))
                            Text("Send", fontWeight = FontWeight.Bold, color = SeleneNight)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent activity", fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = onRefresh) {
                        if (refreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SeleneAmethyst, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = SeleneTextDim)
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SeleneSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = SeleneTextDim)
                            Spacer(Modifier.height(8.dp))
                            Text("No transactions yet", color = SeleneTextDim)
                        }
                    }
                }
            } else {
                items(history, key = { it.signature }) { item ->
                    HistoryRow(item, historyStatuses[item.signature] ?: true)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: SignatureInfo, success: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SeleneSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (success) Color(0xFF173B34) else Color(0xFF3B1717)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (success) Icons.Filled.Check else Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = if (success) SeleneMint else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (success) "Confirmed" else "Failed",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    item.signature,
                    fontFamily = FontFamily.Monospace,
                    color = SeleneTextDim,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("slot ${item.slot}", color = SeleneTextDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SendDialog(
    onDismiss: () -> Unit,
    onSend: (Asset, String, String) -> Unit,
    error: String?
) {
    var asset by remember { mutableStateOf(Asset.SOL) }
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        if (error != null) sending = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SeleneSurface,
        title = { Text("Send", color = SeleneText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Asset.values().forEach { a ->
                        OutlinedButton(
                            onClick = { asset = a },
                            enabled = !sending,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (asset == a) SeleneAmethyst.copy(alpha = 0.25f) else Color.Transparent,
                                contentColor = if (asset == a) SeleneAmethyst else SeleneTextDim
                            )
                        ) {
                            Text(a.label, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Recipient address") },
                    placeholder = { Text("Solana address (base58)") },
                    singleLine = true,
                    enabled = !sending,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    enabled = !sending,
                    keyboardOptions = KeyboardOptionsAlias(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            if (sending) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = SeleneAmethyst, strokeWidth = 3.dp)
            } else {
                Button(
                    onClick = {
                        sending = true
                        onSend(asset, recipient, amount)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeleneAmethyst)
                ) {
                    Text("Send", color = SeleneNight, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !sending) {
                Text("Cancel", color = SeleneTextDim)
            }
        }
    )
}

private fun parseAddress(text: String): ByteArray? {
    val trimmed = text.trim()
    if (trimmed.length != 44) return null
    return try {
        Base58.decodeToByteArray(trimmed).takeIf { it.size == 32 }
    } catch (_: Exception) {
        null
    }
}

private fun positiveLamports(text: String): Long {
    val value = BigDecimal(text.trim())
    require(value > BigDecimal.ZERO) { "Amount must be positive" }
    val lamports = value * BigDecimal.valueOf(LAMPORTS_PER_SOL)
    require(lamports <= BigDecimal.valueOf(Long.MAX_VALUE)) { "Amount too large" }
    return lamports.longValueExact()
}

private fun positiveUsdc(text: String): Long {
    val value = BigDecimal(text.trim())
    require(value > BigDecimal.ZERO) { "Amount must be positive" }
    val units = value * BigDecimal.valueOf(USDC_DECIMALS)
    require(units < BigDecimal.valueOf(1_000_000_000_000L)) { "Amount too large" }
    return units.longValueExact()
}