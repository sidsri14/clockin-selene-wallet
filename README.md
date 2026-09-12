# Selene — A Privacy-First Solana Wallet on Mobile

> **CLOCK IN Hackathon entry** · Solana Mobile Stack built with **Mobile Wallet Adapter** · Jetpack Compose · Kotlin

Selene is a self-custody Solana wallet built for **Solana smartphones and Mobile Wallet Adapter (MWA)** apps. It manages SOL and USDC with one hard rule: **your keys never touch the phone**. Every transaction is authorized inside your own wallet app (Phantom, Solflare, Solana Seeker, and any MWA-compatible wallet) over a secure local WebSocket association — Selene sends unsigned transaction payloads and receives back signed, finalized transactions.

The design language — a quiet crescent moon on a deep night sky — reflects the product thesis: **the best wallet is the one you don't have to think about**, one that protects your financial privacy by keeping keys, addresses, and activity minimal and off-device.

---

## Why Selene?

- **Keys never leave your wallet.** No seed phrases, no private keys, no keystore on disk. Selene speaks the open Mobile Wallet Adapter protocol and lets your existing wallet do the signing.
- **Simple by design.** One screen. Balance, send, activity. Nothing else.
- **Private by construction.** No telemetry, no analytics, no remote config. The app only talks to your wallet and the public Solana RPC.
- **Mobile-first Solana.** Built for the Solana Mobile Stack ecosystem and smartphones (Seeker) — the next billion users arrive on mobile, and they arrive with a wallet already in their pocket.

## Features

| Feature | Implementation |
|---|---|
| MWA connect | `LocalAssociationScenario` — local WebSocket session, launched via association intent, 90s grace window |
| SOL approvals | Legacy transaction compiled in-app, authorized via wallet's `signAndSendTransactions` |
| USDC (SPL) transfers | Token Program v3 transfer, source/destination resolved via `getTokenAccountsByOwner` |
| Balances | Mainnet RPC (`api.mainnet-beta.solana.com`) — SOL via `getBalance`, USDC via parsed token accounts |
| Activity log | `getSignaturesForAddress` + `getSignatureStatuses` with per-tx confirmed/failed state |
| Address UX | Copy-to-clipboard chip, truncated monospace display, 32-byte base58 validation |
| Custom launcher | Night-sky adaptive icon (API 26+) with PNG fallbacks (API 24+) |

## Security model

1. **No key material on device.** Key storage is the *wallet's* job (its secure enclave/keystore).
2. **Local-only transport.** The association WebSocket binds to a loopback port; the intent handshake relays the port/session to the wallet app.
3. **Explicit per-tx consent.** The user reviews and approves every transaction in their own wallet UI before signing.
4. **Read-only RPC surface.** Balance and activity queries need only the public key; transaction building needs only the recent blockhash.

## Tech stack

- **Kotlin 2.0.21** · **Jetpack Compose** (BOM 2024.12.01, Material 3)
- **AGP 8.10.1** · Gradle 8.11.1 · compileSdk/targetSdk **36** · minSdk **24**
- **Solana Mobile Stack**: `com.solanamobile:mobile-wallet-adapter-clientlib:2.0.8`
- JSON-RPC over `HttpURLConnection`, coroutines on `Dispatchers.IO`

## Project layout

```
app/src/main/java/dev/selene/wallet/
├── MainActivity.kt               # entry point
├── core/
│   ├── Base58.kt                 # base58 encode/decode
│   ├── SolanaRpc.kt              # getBalance / token accounts / blockhash / history
│   └── TransactionBuilder.kt     # legacy messages: SOL transfer, SPL Token Program v3 transfer
├── wallet/
│   └── MwaWallet.kt              # LocalAssociationScenario lifecycle, authorize, signAndSend
└── ui/
    ├── SeleneTheme.kt            # night palette (amethyst/mint on deep navy)
    └── SeleneApp.kt              # state machine, dashboard, send dialog
```

## Build it

```bash
# requires JDK 17 and Android SDK (platform 36)
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Build with the bundled wrapper; configure your SDK path via `local.properties` (`sdk.dir=D:/Android/Sdk`).

## Run it

1. Install a MWA wallet (Phantom, Solflare — or use a Solana **Seeker**/smartphone).
2. Open **Selene** → **Connect wallet**.
3. Approve the association + authorization in your wallet.
4. Your SOL & USDC balances load; hit **Send** to authorize an on-chain transfer from the safety of your wallet.

## Roadmap

- **Auto token-account creation** (ATA PDAs) so recipients without USDC accounts can be paid directly
- **Versioned transactions** (v0) and priority fees
- **USDC quote + fiat display**, memo support
- **Stealth privacy mode** — toggle to hide balances/activity on the dashboard (upstream of the StealthShield privacy program)
- **Web3Auth/embedded-key onboarding path** for non-MWA phones

## Demo

- Video: `docs/demo-script.md` (shoot script)
- Pitch: `docs/pitch-deck.md`

## License

MIT.