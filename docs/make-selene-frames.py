"""Render Selene CLOCK IN demo frames (1920x1080) to PNG slides."""
import os
from PIL import Image, ImageDraw, ImageFont

W, H = 1920, 1080
OUT = os.path.join(os.path.dirname(__file__), "frames")
os.makedirs(OUT, exist_ok=True)

BG = (15, 18, 30)
CARD = (26, 32, 52)
TEAL = (52, 211, 153)
VIOLET = (139, 92, 246)
RED = (251, 113, 133)
WHITE = (241, 245, 249)
GREY = (148, 163, 184)
AMBER = (251, 191, 36)

FONTS = {
    "mono": r"C:\Windows\Fonts\consola.ttf",
    "mono_b": r"C:\Windows\Fonts\consolab.ttf",
    "sans": r"C:\Windows\Fonts\segoeui.ttf",
    "sans_b": r"C:\Windows\Fonts\segoeuib.ttf",
    "sans_l": r"C:\Windows\Fonts\segoeuisl.ttf",
}


def font(kind, size):
    p = FONTS.get(kind, "")
    if p and os.path.exists(p):
        return ImageFont.truetype(p, size)
    return ImageFont.load_default()


def bg(d):
    d.rectangle([0, 0, W, H], fill=BG)


def accent(d):
    d.rectangle([0, 0, W, 10], fill=VIOLET)


def scene(d, label, idx, total=7):
    d.text((64, 56), label, font=font("mono_b", 26), fill=TEAL)
    d.text((W - 64 - 200, 56), f"{idx}/{total}", font=font("mono", 26), fill=GREY)


def card(d, x, y, w, h, fill=CARD):
    d.rounded_rectangle([x, y, x + w, y + h], radius=18, fill=fill, outline=(55, 66, 94), width=2)


def title(d):
    accent(d)
    d.text((W // 2, 300), "SELENE", font=font("sans_b", 110), fill=WHITE, anchor="mm")
    d.text((W // 2, 415), "A privacy-first mobile Solana wallet", font=font("sans_l", 46), fill=VIOLET, anchor="mm")
    d.text((W // 2, 500), "Your keys never touch the phone", font=font("sans", 36), fill=GREY, anchor="mm")
    d.rounded_rectangle([W // 2 - 320, 560, W // 2 + 320, 636], radius=38, outline=TEAL, width=3)
    d.text((W // 2, 598), "github.com/sidsri14/clockin-selene-wallet", font=font("mono", 28), fill=WHITE, anchor="mm")
    d.text((W // 2, 730), "CLOCK IN 2026 \u00b7 Solana Mobile \u00b7 $135K", font=font("sans_b", 30), fill=GREY, anchor="mm")
    d.text((W // 2, 800), "Native Android APK \u00b7 Mobile Wallet Adapter \u00b7 Solana Stack", font=font("mono", 26), fill=TEAL, anchor="mm")


def problem(d):
    scene(d, "PROBLEM", 1)
    d.text((W // 2, 170), "The phone you carry is the worst place", font=font("sans_b", 52), fill=WHITE, anchor="mm")
    d.text((W // 2, 245), "for the keys you care about most.", font=font("sans_b", 52), fill=WHITE, anchor="mm")
    card(d, 260, 330, 1400, 360)
    d.text((340, 385), "hot wallet on the device", font=font("mono_b", 30), fill=GREY)
    d.text((340, 445), "seed phrases live in the same phone that gets", font=font("mono", 28), fill=WHITE)
    d.text((340, 495), "lost, resold, backed up to cloud, or tapped by spyware", font=font("mono", 28), fill=WHITE)
    d.rounded_rectangle([340, 560, 1460, 630], radius=14, fill=(43, 30, 40))
    d.text((370, 595), "one compromise = full wallet drained", font=font("mono_b", 28), fill=RED)
    d.text((W // 2, 830), "users shouldn't have to choose between convenience and custody", font=font("sans", 32), fill=GREY, anchor="mm")


def mwa(d):
    scene(d, "SOLUTION \u00b7 MOBILE WALLET ADAPTER", 2)
    d.text((W // 2, 170), "Keys stay where they want to live — in the wallet app.", font=font("sans_b", 46), fill=WHITE, anchor="mm")
    card(d, 120, 260, 830, 420)
    d.text((160, 310), "SELENE (this app)", font=font("mono_b", 28), fill=TEAL)
    d.text((160, 370), "compiles the transaction", font=font("mono", 26), fill=WHITE)
    d.text((160, 425), "without ever holding the key", font=font("mono", 26), fill=WHITE)
    d.text((160, 480), "WK connect \u00b7 LocalAssociationScenario", font=font("mono", 22), fill=GREY)
    d.text((160, 540), "90s grace window \u00b7 association intent", font=font("mono", 22), fill=GREY)
    card(d, 980, 260, 820, 420)
    d.text((1020, 310), "PHANTOM / SOLFLARE", font=font("mono_b", 28), fill=VIOLET)
    d.text((1020, 370), "holds the seed \u00b7 shows ka approval UI", font=font("mono", 26), fill=WHITE)
    d.text((1020, 425), "signs and returns signed txs", font=font("mono", 26), fill=WHITE)
    d.text((1020, 480), "signAndSendTransactions", font=font("mono", 22), fill=GREY)
    d.text((1020, 540), "wallet does the signing \u2014 Selene never sees it", font=font("mono", 22), fill=TEAL)
    d.rounded_rectangle([W // 2 - 250, 720, W // 2 + 250, 800], radius=34, outline=TEAL, width=3)
    d.text((W // 2, 760), "local WebSocket \u2014 nothing leaves the device", font=font("mono_b", 28), fill=TEAL, anchor="mm")
    d.text((W // 2, 900), "keys never exist in Selene: no seed, no keystore, no cloud", font=font("sans", 30), fill=GREY, anchor="mm")


def sol(d):
    scene(d, "FEATURE 1 \u00b7 SOL TRANSFER", 3)
    d.text((W // 2, 170), "Send SOL \u2014 compiled here, approved there.", font=font("sans_b", 46), fill=WHITE, anchor="mm")
    card(d, 260, 260, 1400, 300)
    d.text((340, 320), "amount", font=font("mono_b", 28), fill=GREY)
    d.text((340, 380), "1.25 SOL", font=font("mono_b", 44), fill=WHITE)
    d.text((1220, 380), "legacy transfer", font=font("mono", 26), fill=TEAL)
    d.text((1220, 440), "byte-exact \u00b7 fee-estimated", font=font("mono", 22), fill=GREY)
    card(d, 260, 590, 1400, 180)
    d.text((340, 640), "recipient balloon wallet", font=font("mono_b", 28), fill=GREY)
    d.text((340, 700), "8uQE...shortened xKayp2jDQHDmwB6mkD5eYGGY3FQfLy", font=font("mono", 26), fill=WHITE)
    d.text((W // 2, 870), "in-wallet approve \u2192 sign \u2192 sent. Never on a server.", font=font("sans", 30), fill=TEAL, anchor="mm")


def usdc(d):
    scene(d, "FEATURE 2 \u00b7 USDC + AUTO ATA", 4)
    d.text((W // 2, 170), "USDC to anyone \u2014 even if they have no token account yet.", font=font("sans_b", 44), fill=WHITE, anchor="mm")
    card(d, 180, 260, 780, 330)
    d.text((220, 315), "resolve recipient accounts", font=font("mono_b", 28), fill=GREY)
    d.text((220, 375), "getTokenAccountsByOwner", font=font("mono", 25), fill=WHITE)
    d.text((220, 435), "no USDC account found \u2192 ATA missing", font=font("mono", 25), fill=AMBER)
    d.text((220, 490), "Token Program v3 \u00b7 direct", font=font("mono", 22), fill=GREY)
    card(d, 990, 260, 750, 330)
    d.text((1030, 315), "create it on the fly", font=font("mono_b", 28), fill=TEAL)
    d.text((1030, 375), "PDA-backed associated token account", font=font("mono", 25), fill=WHITE)
    d.text((1030, 435), "create + transfer in ONE message", font=font("mono", 25), fill=WHITE)
    d.text((1030, 490), "byte-exact \u00b7 no two-step UX", font=font("mono", 22), fill=GREY)
    d.rounded_rectangle([W // 2 - 420, 660, W // 2 + 420, 745], radius=34, outline=TEAL, width=3)
    d.text((W // 2, 702), "send ZSP to a fresh wallet \u2014 it just works", font=font("mono_b", 28), fill=TEAL, anchor="mm")
    d.text((W // 2, 870), "the account that makes USDC-to-anyone possible \u2014 on mobile", font=font("sans", 30), fill=GREY, anchor="mm")


def privacy(d):
    scene(d, "PRIVACY BY CONSTRUCTION", 5)
    d.text((W // 2, 170), "Zero telemetry. Zero surprises.", font=font("sans_b", 52), fill=WHITE, anchor="mm")
    rows = [
        ("telemetry / analytics / crash reporting", "NOT PRESENT", RED),
        ("remote config / server calls", "NOT PRESENT", RED),
        ("seed / mnemonic / private key storage", "NOT PRESENT", RED),
        ("app-to-app data (MWA association only)", "IN-MEMORY, LOCAL", TEAL),
        ("network endpoints", "wallet apps + public RPC", TEAL),
    ]
    card(d, 200, 240, 1520, 400)
    y = 300
    for label, val, col in rows:
        d.text((280, y), label, font=font("mono", 26), fill=GREY)
        d.text((1380, y), val, font=font("mono_b", 26), fill=col, anchor="ra")
        y += 66
    d.text((W // 2, 720), "privacy is not a feature — it is the absence of features", font=font("sans_b", 36), fill=TEAL, anchor="mm")
    d.text((W // 2, 800), "you can read the whole app and verify the claim", font=font("sans", 28), fill=GREY, anchor="mm")
    d.text((W // 2, 880), "APK: app/build/outputs/apk/debug/app-debug.apk \u00b7 16.8 MB", font=font("mono", 26), fill=GREY, anchor="mm")


def close(d):
    scene(d, "BOTTOM LINE", 6)
    d.text((W // 2, 220), "The wallet for the Solana smartphone era", font=font("sans_b", 50), fill=WHITE, anchor="mm")
    card(d, 260, 320, 1400, 260)
    d.text((340, 385), "mobile-first \u2014 not a web wrapper", font=font("mono_b", 30), fill=TEAL)
    d.text((340, 445), "deep Mobile Wallet Adapter integration (LocalAssociation)", font=font("mono", 28), fill=WHITE)
    d.text((340, 505), "real Solana network interactions \u00b7 SOL + USDC + auto-ATA", font=font("mono", 28), fill=WHITE)
    d.text((W // 2, 700), "install the APK. approve from your wallet. done.", font=font("sans_b", 40), fill=WHITE, anchor="mm")
    d.text((W // 2, 860), "CLOCK IN \u00b7 github.com/sidsri14/clockin-selene-wallet", font=font("mono", 30), fill=TEAL, anchor="mm")


SLIDES = [title, problem, mwa, sol, usdc, privacy, close]

for i, fn in enumerate(SLIDES, 1):
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    bg(d)
    fn(d)
    p = os.path.join(OUT, f"slide-{i:02d}.png")
    img.save(p)
    print("wrote", p)
print("done", len(SLIDES))