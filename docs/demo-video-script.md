# Selene — Demo Video Script (~90s, 1080p)

Requires an Android device/emulator with MWA-enabled wallet. NOTE (user constraint): the
machine has had its Android emulator stack removed. Options: (a) record on a physical
Solana Seeker/Android phone + Phantom/Solflare MWA wallet; (b) reuse the CLOCK IN
submission screen recording if it exists; (c) alternate: static walkthrough with mock
frames rendered from Compose previews. Scene timings assume (a).

## Scene 1 (0-12s) — Pitch
- **Visual:** App splash — crescent moon on night sky; then the one-screen wallet.
- **Voice:** "Selene is a wallet that keeps your keys exactly where they belong — in
  your wallet. Self-custody, mobile-first, zero telemetry."

## Scene 2 (12-35s) — Connect (MWA)
- **Visual:** Tap Connect → association intent launches Phantom → local WebSocket pair →
  "Connected" state.
- **Voice:** "One tap. Selene speaks Mobile Wallet Adapter, associates over a local
  connection, and never holds a private key. Keys stay in your signing wallet."

## Scene 3 (35-60s) — SOL + USDC sends
- **Visual:** Send SOL to a test address, confirm inside Phantom, finalized state. Then
  USDC send to a fresh address that has no USDC account.
- **Voice:** "SOL in, USDC next — and watch this: the recipient has no token account yet.
  Selene creates it on the fly, in the same byte-exact transaction."

## Scene 4 (60-80s) — ATA autocreation + privacy
- **Visual:** Explorer/terminal showing ATA creation + transfer in one message; then show
  "no analytics, no remote config" under Settings.
- **Voice:** "One message, account created and funded. And that's the whole point —
  nothing to leak, nothing to scrape. Your wallet, your keys, your business."

## Scene 5 (80-90s) — Close
- Repo + APK link on screen.
- **Voice:** "Selene — the best wallet is the one you don't have to think about."

## Rendering hint
- 1080p, <2 min, <25 MB. Upload to Loom/YouTube, link on Colosseum form.
- If no device available today, record static walkthrough and note the APK is
  installable for judges' own testing.