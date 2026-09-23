# CLOCK IN — Selene submission text (paste into Solana Mobile hackathon form)

## Project Name
**Selene** — a privacy-first mobile Solana wallet. Your keys never touch the phone.

## Elevator pitch (2–3 sentences)
Selene is the wallet for the Solana smartphone era: self-custody, one screen, zero
telemetry. Nobody holds your keys on-device — every transaction is authorized inside your
existing Mobile Wallet Adapter wallet (Phantom, Solflare, Seeker) over a secure local
WebSocket association, so your seed never exists on the phone you carry everywhere.

## What I built (evidence > claims)
- **Native Android APK (16.8 MB)** — Jetpack Compose + Kotlin, single-screen
  balance/send/activity UX. It installs and runs; judges start the app, not a description.
- **Mobile Wallet Adapter depth** — LocalAssociationScenario integration: local WebSocket,
  association intent for in-wallet confirmation, 90-second grace window.
- **Real Solana interactions** — SOL legacy transfers, USDC via Token Program v3
  (token accounts resolved via getTokenAccountsByOwner), and **automatic ATA creation**:
  sending USDC to a recipient who has no USDC account mints their PDA-backed ATA and
  transfers in a single byte-exact message.
- **Privacy by construction** — no telemetry, no analytics, no remote config. The app
  talks only to the connected wallet and the public RPC.

## APK (required)
`app/build/outputs/apk/debug/app-debug.apk` in the repo, or direct download link above.

## GitHub repository
https://github.com/sidsri14/clockin-selene-wallet

## Demo video
Pitch deck: `docs/Selene-CLOCKIN-Deck.pdf`. Video link: *(paste YT/Loom — script at
`docs/demo-video-script.md`)*

## Why it wins
CLOCK IN explicitly rewards deep Mobile Stack integration over web wrappers. Selene is the
deep end: a genuine LocalAssociationScenario session, real on-chain SPL transfers, and
automatic token-account creation — plus a privacy story (no seed on device) that is
native to mobile and impossible on desktop. That is the exact bar the judges set:
"functional Android application with meaningful mobile-specific development."