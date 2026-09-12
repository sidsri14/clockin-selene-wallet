# Selene Wallet — Pitch Deck (8 slides)

> Presenter deck for CLOCK IN judging. Fields map to the judging rubric: **Product-market fit 25%**, **UX & polish 25%**, plus engineering quality & storytelling.

---

## Slide 1 — Title

**Selene**
A privacy-first Solana wallet for Solana smartphones

*"The best wallet is the one you don't have to think about."*

Subline: Built with Solana Mobile Stack · Mobile Wallet Adapter · Jetpack Compose · Kotlin

---

## Slide 2 — The problem (Market)

> Target: **Product-market fit (25%)**

- Mobile Solana users keep money in **MWA wallets** — with keys in the wallet's secure enclave.
- Most "wallets" rebuild key storage and add **attack surface**, or expose your whole on-chain footprint.
- Users (esp. **on Seeker & Solana smartphones**) want to **hold & send** on-chain assets with **zero key exposure** and **minimal data traces**.

**Pain:** "I want to use my Solana smartphone without trusting another app with my keys or telemetry."

---

## Slide 3 — The insight (Differentiation)

> Target: engineering & vision

- **Keys never touch the dApp.** Selene is the *interface*; MWA is the mobility layer; the wallet holds custody.
- **Local-by-construction:** loopback WebSocket association, no cloud relay.
- **Privacy = default:** no analytics, no telemetry, no remote config — trivially auditable (it's open source).

Claim: *"The reference interaction model for the mobile Solana wallet of the next era."*

---

## Slide 4 — The product (1/2)

- **MWA connect** — one tap → association intent → wallet approval (Westworld-grade UX)
- **Dashboard** — live SOL + USDC balances from mainnet RPC, copyable address chip
- **Activity log** — recent signatures with confirmed/failed status from chain

---

## Slide 5 — The product (2/2) — Sending

- **SOL transfers** — legacy transaction built in-app (System Program)
- **USDC transfers** — true SPL Token Program v3 transfers, source/dest resolved from `getTokenAccountsByOwner`
- **Consent stays in the wallet** — `signAndSendTransactions` → your wallet signs & submits

Include a mock: Send sheet with asset toggle + recipient + amount.

---

## Slide 6 — Engineering

- Kotlin 2.0.21 · Compose (Material 3) · AGP 8.10.1 · Gradle 8.11.1 · **compileSdk 36 / minSdk 24**
- **Solana Mobile Stack**: `mobile-wallet-adapter-clientlib:2.0.8` (`LocalAssociationScenario`, `authorize`, `signAndSendTransactions`)
- Custom base58, short-vec length-prefix message compiler, u64 encoding — no SDK bloat for the critical path
- 3-module layering: `core` (chain primitives) / `wallet` (MWA lifecycle) / `ui` (Compose)

---

## Slide 7 — Evidence & roadmap

- ✅ Installs & runs: build verified, APK produced, launches on Android 36 emulator
- ✅ Real RPC data flow wired end-to-end
- → **Now:** ATA auto-open for first-time USDC recipients, v0 transactions, priority fees
- → **Next:** stealth privacy mode (dapp-side of StealthShield), fiat/USD quote, chain-swap

---

## Slide 8 — Close

**Selene — hold. send. stay private.**

- Demo video (QR to the 90s recording)
- Repo: `github.com/sidsri14/clockin-selene-wallet` (APK in `app/build/outputs` + CI note)
- Built for CLOCK IN, Sept 2026, by Sid Sri — Solana Mobile Stack entry