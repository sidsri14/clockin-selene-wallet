# Selene Wallet — Demo Video Script (60–90s)

> Format: 1080×1920 portrait, screen recording + narration. Keep cuts tight; every shot moves the story forward.

## Scene 1 — Hook (0–8s)

**Shot:** Launcher slide → Selene icon bounces in on night sky.
**Text overlay:** "Your keys never touch this phone."
**Narration:** "Every wallet on your phone wants you to trust it with your seed phrase. Selene doesn't need it."

## Scene 2 — The problem (8–18s)

**Shot:** Generic wallet app showing a 12-word seed on screen, address repeated everywhere.
**Text overlay:** "Exposure = risk. Privacy = freedom."
**Narration:** "Your seed phrase, your addresses, your whole on-chain history — most wallets expose all of it. That's the attack surface."

## Scene 3 — Connect (18–32s)

**Shot (screen recording):** Open Selene → **Connect wallet** → Android wallet chooser → approve association in wallet.
**Text overlay:** "Solana Mobile Stack · Mobile Wallet Adapter"
**Narration:** "Selene is built on Solana Mobile Stack. Tap connect, approve in your wallet — and the session is established over a secure local link. No keys move. Selene doesn't store them; your wallet keeps them in its secure enclave."

## Scene 4 — Dashboard (32–45s)

**Shot (screen recording):** Balances load — 0.xxxx SOL card + USDC line, activity list populates.
**Text overlay:** "Live mainnet balances."
**Narration:** "Your mainnet balances stream in — SOL, collected via RPC, and USDC from your token accounts. All read-only. Selene only ever asks for your public key."

## Scene 5 — Send SOL (45–58s)

**Shot (screen recording):** Send → asset SOL → paste recipient → amount → **Send** → wallet approval dialog → "Sent!" → balance refreshes, activity updates.
**Text overlay:** "Authorize in your wallet. Sent on-chain."
**Narration:** "Sending is where Selene proves it. We build the transaction, hand it to your wallet, and you make the final decision — in your wallet. When you approve, it's signed and submitted. When it lands, the activity log updates."

## Scene 6 — Send USDC (58–72s)

**Shot (screen recording):** Send → asset USDC → recipient with an existing USDC account → approve in wallet → confirmed.
**Text overlay:** "SPL Token Program · v3 transfers"
**Narration:** "USDC works the same way — proper SPL transfers with the Token Program, source and destination resolved from your token accounts on-chain."

## Scene 7 — Privacy pitch (72–85s)

**Shot:** Slow zoom on the crescent moon logo, tabulating empty-state dashboard.
**Text overlay:** "No analytics. No telemetry. No remote config."
**Narration:** "No analytics, no telemetry, no remote config. Selene is the quietest app on your phone — privacy by construction, for the Solana smartphone era."

## Scene 8 — Close (85–90s)

**Shot:** Wordmark "Selene" over night sky.
**Text overlay:** "Selene · Hold. Send. Disappear." (or "Hold. Send. Stay private.")
**Narration:** "Selene. The best wallet is the one you don't have to think about."

---

## Recording tips

- Record at 1080×1920; use a device with a **valorized MWA wallet installed** (Phantom/Solflare on emulator, or a Seeker smartphone for the bravo shot).
- For the demo, a **devnet** USDC burner account is fine — point `SolanaRpc.DEFAULT` at a devnet endpoint and show 1 real on-chain send; disclose "devnet" in a corner label if you do.
- Keep on-screen text ≥ 32dp tall so it's legible when compressed.
- Caption every approval dialog with a callout arrow (e.g., "✋ final say lives here").