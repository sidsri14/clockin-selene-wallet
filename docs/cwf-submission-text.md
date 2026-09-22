# Selene — CWF Submission Text (Solana track + Robinhood Chain b-eligibility)

## One-liner (140-char friendly)
A privacy-first Solana wallet on mobile — your keys never touch the phone.

## Short description (200 words max)
Selene is a self-custody Solana wallet built on the Solana Mobile Stack with Mobile Wallet
Adapter, Jetpack Compose and Kotlin. It manages SOL and USDC under one hard rule: keys
never live on the phone. Every transaction is authorized inside the user's existing MWA
wallet (Phantom, Solflare, Solana Seeker, and any MWA-compatible wallet) over a secure
local WebSocket association — Selene sends unsigned transaction payloads and receives
back signed, finalized transactions. No seed, no private key, no keystore on disk. One
screen: balance, send, activity. No telemetry, no analytics, no remote config — the app
only talks to your wallet and the public Solana RPC. Ships as an installable 16.1 MB APK
with working SOL approvals, USDC SPL transfers, and automatic associated-token-account
creation for recipients who don't yet have a USDC account.

## Long description / "What others can build"
Libraries for MWA local-association flows; reference Compose implementation of
privacy-by-construction wallet UX; the ATA-autocreate transfer path is a reusable pattern
for any single-token send flow on Solana Mobile Stack.

## Track
Solana track (mobile-first; Solana Mobile Stack native). Also eligible for the general
pool view and a possible Robinhood Chain cross-listing.

## Repo link
https://github.com/sidsri14/clockin-selene-wallet

## APK (judges can install)
app/build/outputs/apk/debug/app-debug.apk — 16.1 MB

## Demo video (see demo-video-script.md)
~90s: connect via MWA → SOL send → USDC send → ATA autcreation → privacy close.