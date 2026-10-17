#!/usr/bin/env python3
"""
Generates every raster asset used by IDE Ultimate:

* app/src/main/res/drawable-nodpi/splash_nebula.webp  — splash/dashboard backdrop
* app/src/main/res/mipmap-*/ic_launcher*.png          — legacy launcher icons (API < 26)
* docs/ic_launcher_512.png, docs/ic_launcher_round_512.png
* docs/splash_preview.png                            — what the splash screen looks like
* docs/banner.png                                    — README hero image

The Android vector drawables are the source of truth for the adaptive icon;
this script mirrors the same geometry for the legacy PNG fallbacks.

Requirements:  pip install pillow numpy arabic-reshaper python-bidi
Usage:         python tools/generate_assets.py
"""

from __future__ import annotations

import math
import random
from pathlib import Path

import numpy as np
from arabic_reshaper import reshape
from bidi.algorithm import get_display
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FONT_DIR = ROOT / "app" / "src" / "main" / "res" / "font"
SOURCE_NEBULA = ROOT / "design" / "nebula_source.png"
RES = ROOT / "app" / "src" / "main" / "res"
DOCS = ROOT / "docs"

DEEP_NAVY = (5, 8, 22)
MIDNIGHT = (10, 15, 36)
VIOLET = (124, 58, 237)
VIOLET_SOFT = (167, 139, 250)
LILAC = (196, 181, 253)
FUCHSIA = (240, 171, 252)
CYAN = (103, 232, 249)
TEAL = (94, 234, 212)
INK = (237, 239, 255)
MUTED = (154, 163, 199)

SCALE = 3  # supersampling for crisp shapes


# --------------------------------------------------------------------------- #
# Helpers
# --------------------------------------------------------------------------- #
def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONT_DIR / name), size)


def ar(text: str) -> str:
    """Reshape Arabic and put it in visual order for Pillow (no raqm needed)."""
    return get_display(reshape(text))


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(len(a)))


def gradient_color(t: float):
    stops = [(0.0, CYAN), (0.42, VIOLET_SOFT), (0.78, LILAC), (1.0, FUCHSIA)]
    t = max(0.0, min(1.0, t))
    for i in range(len(stops) - 1):
        t0, c0 = stops[i]
        t1, c1 = stops[i + 1]
        if t <= t1:
            local = 0.0 if t1 == t0 else (t - t0) / (t1 - t0)
            return lerp(c0, c1, local)
    return stops[-1][1]


def linear_gradient_rgba(size, start, end, colors, horizontal=False):
    w, h = size
    arr = np.zeros((h, w, 4), dtype=np.uint8)
    if horizontal:
        t = np.linspace(0, 1, w)[None, :].repeat(h, axis=0)
    else:
        t = np.linspace(0, 1, h)[:, None].repeat(w, axis=1)
    stops = np.linspace(0, 1, len(colors))
    for channel in range(3):
        values = np.array([c[channel] for c in colors])
        arr[..., channel] = np.interp(t, stops, values).astype(np.uint8)
    arr[..., 3] = 255
    return Image.fromarray(arr, "RGBA")


def radial_rgba(size, center, radius, color, max_alpha=255, power=1.8):
    w, h = size
    yy, xx = np.mgrid[0:h, 0:w]
    dist = np.sqrt((xx - center[0]) ** 2 + (yy - center[1]) ** 2) / max(radius, 1)
    alpha = np.clip(1 - dist, 0, 1) ** power * max_alpha
    arr = np.zeros((h, w, 4), dtype=np.uint8)
    arr[..., 0] = color[0]
    arr[..., 1] = color[1]
    arr[..., 2] = color[2]
    arr[..., 3] = alpha.astype(np.uint8)
    return Image.fromarray(arr, "RGBA")


def paste_center(base: Image.Image, overlay: Image.Image, center):
    x = int(center[0] - overlay.width / 2)
    y = int(center[1] - overlay.height / 2)
    base.alpha_composite(overlay, (x, y))


def rounded_mask(size, radius):
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=radius, fill=255)
    return mask


def circle_mask(size):
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size[0] - 1, size[1] - 1), fill=255)
    return mask


def add_stars(draw: ImageDraw.ImageDraw, size, count=110, seed=7, scale=1.0):
    rng = random.Random(seed)
    w, h = size
    for _ in range(count):
        x = rng.uniform(0, w)
        y = rng.uniform(0, h)
        r = rng.uniform(0.6, 2.4) * scale
        alpha = int(rng.uniform(50, 230))
        draw.ellipse((x - r, y - r, x + r, y + r), fill=(255, 255, 255, alpha))


def gradient_text(size, text, font_obj, start_color, end_color, letterspacing=0):
    """Render text into a transparent image with a horizontal gradient fill."""
    tmp = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(tmp)
    # textbbox ignores trailing spaces; draw char by char for letter spacing
    total_w = 0
    widths = []
    for ch in text:
        bbox = d.textbbox((0, 0), ch, font=font_obj)
        w = bbox[2] - bbox[0]
        widths.append(w)
        total_w += w + letterspacing
    total_w -= letterspacing if text else 0
    x = (size[0] - total_w) / 2
    y = (size[1] - font_obj.size) / 2 - font_obj.size * 0.12
    for ch, w in zip(text, widths):
        d.text((x, y), ch, font=font_obj, fill=(255, 255, 255, 255))
        x += w + letterspacing

    gradient = linear_gradient_rgba(size, None, None, [start_color, end_color], horizontal=True)
    gradient.putalpha(tmp.getchannel("A"))
    return gradient


# --------------------------------------------------------------------------- #
# Logo geometry (mirrors the Android vector drawable)
# --------------------------------------------------------------------------- #
def hex_points(cx, cy, r, angle=-90):
    return [
        (
            cx + r * math.cos(math.radians(angle + 60 * i)),
            cy + r * math.sin(math.radians(angle + 60 * i)),
        )
        for i in range(6)
    ]


def draw_gradient_polygon_outline(draw, points, width, closed=True, smooth=0.0):
    """Gradient stroke by drawing many short segments."""
    path = list(points) + ([points[0]] if closed else [])
    segments = max(24, int(len(path) * 12))
    total = len(path) - 1
    for i in range(segments):
        t0 = i / segments
        t1 = (i + 1) / segments
        a = sample_polyline(path, t0)
        b = sample_polyline(path, t1)
        color = gradient_color((t0 + t1) / 2)
        draw.line([a, b], fill=color + (255,), width=width)
        r = width // 2
        draw.ellipse((b[0] - r, b[1] - r, b[0] + r, b[1] + r), fill=color + (255,))


def sample_polyline(path, t):
    total_len = sum(
        math.dist(path[i], path[i + 1]) for i in range(len(path) - 1)
    )
    target = total_len * t
    walked = 0.0
    for i in range(len(path) - 1):
        seg = math.dist(path[i], path[i + 1])
        if walked + seg >= target or i == len(path) - 2:
            local = 0 if seg == 0 else (target - walked) / seg
            return (
                path[i][0] + (path[i + 1][0] - path[i][0]) * local,
                path[i][1] + (path[i + 1][1] - path[i][1]) * local,
            )
        walked += seg
    return path[-1]


def draw_logo(img: Image.Image, cx: float, cy: float, size: float, glow=True):
    """Draw the code-hexagon mark centred at (cx, cy) with a total size."""
    r = size * 0.315
    stroke = size * 0.031

    if glow:
        halo = radial_rgba(
            img.size,
            (cx, cy),
            size * 0.78,
            VIOLET,
            max_alpha=120,
            power=2.2,
        )
        img.alpha_composite(halo)

    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)

    # faint wide halo stroke
    d.polygon(hex_points(cx, cy, r), outline=VIOLET + (60,), width=int(stroke * 2.4))

    # main hexagon
    draw_gradient_polygon_outline(d, hex_points(cx, cy, r), int(stroke))

    # brackets
    bx = r * 0.44
    by = r * 0.42
    tip = r * 0.74
    bw = int(stroke * 1.02)
    d.line([(cx - bx, cy - by), (cx - tip, cy), (cx - bx, cy + by)], fill=CYAN + (255,), width=bw, joint="curve")
    d.line([(cx + bx, cy - by), (cx + tip, cy), (cx + bx, cy + by)], fill=LILAC + (255,), width=bw, joint="curve")

    # slash
    d.line(
        [(cx + r * 0.14, cy - r * 0.46), (cx - r * 0.14, cy + r * 0.46)],
        fill=(255, 255, 255, 235),
        width=max(2, int(stroke * 0.72)),
    )

    img.alpha_composite(layer)


# --------------------------------------------------------------------------- #
# Assets
# --------------------------------------------------------------------------- #
def build_nebula():
    source = Image.open(SOURCE_NEBULA).convert("RGB")
    target_w, target_h = 1080, 1920
    src_ratio = source.width / source.height
    target_ratio = target_w / target_h
    if src_ratio > target_ratio:
        new_h = target_h
        new_w = int(new_h * src_ratio)
    else:
        new_w = target_w
        new_h = int(new_w / src_ratio)
    resized = source.resize((new_w, new_h), Image.LANCZOS)
    left = (new_w - target_w) // 2
    top = (new_h - target_h) // 2
    cropped = resized.crop((left, top, left + target_w, top + target_h))

    arr = np.asarray(cropped).astype(np.float32)
    # deepen the contrast a touch so white UI text always pops
    arr = np.clip((arr - 128) * 1.06 + 122, 0, 255)
    processed = Image.fromarray(arr.astype(np.uint8))

    out = RES / "drawable-nodpi" / "splash_nebula.webp"
    out.parent.mkdir(parents=True, exist_ok=True)
    processed.save(out, "WEBP", quality=90, method=6)
    print(f"  splash_nebula.webp  {out.stat().st_size / 1024:.0f} KB")


def build_icon(size=1024):
    canvas = linear_gradient_rgba(
        (size, size), None, None,
        [(7, 11, 26), (16, 22, 55), (42, 21, 89)],
    )
    # diagonal-ish nebula glow at the bottom
    canvas.alpha_composite(
        radial_rgba((size, size), (size * 0.5, size * 0.92), size * 0.72, VIOLET, 130, 1.7)
    )
    canvas.alpha_composite(
        radial_rgba((size, size), (size * 0.12, size * 0.18), size * 0.55, CYAN, 45, 2.0)
    )

    d = ImageDraw.Draw(canvas)
    add_stars(d, (size, size), count=70, seed=4, scale=size / 360)

    draw_logo(canvas, size / 2, size / 2, size * 0.80)

    # tiny sparkle accents
    for (sx, sy, sr) in [
        (size * 0.18, size * 0.24, size * 0.008),
        (size * 0.84, size * 0.2, size * 0.006),
        (size * 0.88, size * 0.66, size * 0.005),
    ]:
        d.ellipse((sx - sr, sy - sr, sx + sr, sy + sr), fill=(255, 255, 255, 170))

    return canvas


def build_icons():
    icon = build_icon(1024)
    DOCS.mkdir(exist_ok=True)

    rounded = Image.new("RGBA", icon.size, (0, 0, 0, 0))
    rounded.paste(icon, (0, 0), rounded_mask(icon.size, int(icon.width * 0.225)))
    rounded.resize((512, 512), Image.LANCZOS).save(DOCS / "ic_launcher_512.png")

    circular = Image.new("RGBA", icon.size, (0, 0, 0, 0))
    circular.paste(icon, (0, 0), circle_mask(icon.size))
    circular.resize((512, 512), Image.LANCZOS).save(DOCS / "ic_launcher_round_512.png")

    sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for density, px in sizes.items():
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        rounded.resize((px, px), Image.LANCZOS).save(folder / "ic_launcher.png", optimize=True)
        circular.resize((px, px), Image.LANCZOS).save(folder / "ic_launcher_round.png", optimize=True)
        print(f"  mipmap-{density}: {px}px")
    print("  docs/ic_launcher_512.png + round")


def build_splash_preview():
    w, h = 1080, 1920
    base = Image.open(RES / "drawable-nodpi" / "splash_nebula.webp").convert("RGBA")
    base = base.resize((w, h), Image.LANCZOS)

    # vertical scrim
    scrim = Image.new("RGBA", (w, h))
    arr = np.zeros((h, w, 4), dtype=np.uint8)
    stops = [(0.0, 0.86), (0.28, 0.30), (0.62, 0.0), (1.0, 0.92)]
    ys = np.linspace(0, 1, h)
    alphas = np.interp(ys, [s[0] for s in stops], [s[1] for s in stops]) * 255
    arr[..., 0], arr[..., 1], arr[..., 2] = DEEP_NAVY
    arr[..., 3] = alphas[:, None].astype(np.uint8)
    scrim = Image.fromarray(arr, "RGBA")
    base.alpha_composite(scrim)

    # vignette
    base.alpha_composite(
        radial_rgba((w, h), (w / 2, h / 2), max(w, h) * 1.02, DEEP_NAVY, 175, 2.6)
    )

    d = ImageDraw.Draw(base)
    add_stars(d, (w, h), count=90, seed=3, scale=1.0)

    draw_logo(base, w / 2, 710, 468)

    # Wordmark with gradient
    wordmark_font = font("almarai_extrabold.ttf", 84)
    text_layer = Image.new("RGBA", (w, 140), (0, 0, 0, 0))
    td = ImageDraw.Draw(text_layer)
    text = "IDE ULTIMATE"
    spacing = 9
    widths = [td.textbbox((0, 0), ch, font=wordmark_font)[2] - td.textbbox((0, 0), ch, font=wordmark_font)[0] for ch in text]
    total = sum(widths) + spacing * (len(text) - 1)
    x = (w - total) / 2
    y = 20
    for ch, wd in zip(text, widths):
        td.text((x, y), ch, font=wordmark_font, fill=(255, 255, 255, 255))
        x += wd + spacing

    gradient = linear_gradient_rgba((w, 140), None, None, [(255, 255, 255), LILAC, CYAN], horizontal=True)
    gradient.putalpha(text_layer.getchannel("A"))
    glow = gradient.filter(ImageFilter.GaussianBlur(18))
    glow.putalpha(glow.getchannel("A").point(lambda v: int(v * 0.45)))
    base.alpha_composite(glow, (0, 1000))
    base.alpha_composite(gradient, (0, 1000))

    # Tagline
    tag_font = font("almarai_regular.ttf", 42)
    tagline = ar("بيئة تطوير متكاملة… في راحة يدك")
    tw = d.textbbox((0, 0), tagline, font=tag_font)[2]
    d.text(((w - tw) / 2, 1150), tagline, font=tag_font, fill=MUTED + (255,))

    # Progress bar (RTL: fills from the right)
    bar_w, bar_h = 660, 15
    bar_x = (w - bar_w) // 2
    bar_y = 1332
    d.rounded_rectangle((bar_x, bar_y, bar_x + bar_w, bar_y + bar_h), radius=bar_h // 2, fill=(255, 255, 255, 26))

    progress = 0.62
    fill_w = int(bar_w * progress)
    fill = linear_gradient_rgba((fill_w, bar_h), None, None, [CYAN, VIOLET_SOFT, FUCHSIA], horizontal=True)
    bar_glow = fill.filter(ImageFilter.GaussianBlur(14))
    base.alpha_composite(bar_glow, (bar_x + bar_w - fill_w, bar_y - 4))
    base.alpha_composite(fill, (bar_x + bar_w - fill_w, bar_y))

    head_x = bar_x + bar_w - fill_w
    head_glow = radial_rgba((w, h), (head_x, bar_y + bar_h / 2), 46, CYAN, 190, 2.0)
    base.alpha_composite(head_glow)
    d.ellipse((head_x - 7, bar_y + bar_h / 2 - 7, head_x + 7, bar_y + bar_h / 2 + 7), fill=(255, 255, 255, 245))

    # Status + percent
    status_font = font("almarai_regular.ttf", 30)
    mono_font = font("jetbrains_mono_regular.ttf", 30)
    status = ar("تجهيز المعاينة الفورية")
    d.text((bar_x, bar_y + 44), status, font=status_font, fill=MUTED + (255,))
    pct = "62%"
    pw = d.textbbox((0, 0), pct, font=mono_font)[2]
    d.text((bar_x + bar_w - pw, bar_y + 44), pct, font=mono_font, fill=CYAN + (255,))

    # Skip pill (top-left in RTL)
    skip_font = font("almarai_bold.ttf", 27)
    skip = ar("تخطي")
    sw = d.textbbox((0, 0), skip, font=skip_font)[2]
    pill_w, pill_h = sw + 64, 68
    px, py = 66, 70
    d.rounded_rectangle((px, py, px + pill_w, py + pill_h), radius=pill_h // 2, fill=(255, 255, 255, 20), outline=(255, 255, 255, 45), width=2)
    d.text((px + (pill_w - sw) / 2, py + 16), skip, font=skip_font, fill=INK + (255,))

    # Bottom branding
    mono_small = font("jetbrains_mono_medium.ttf", 27)
    powered = "KOTLIN  •  JETPACK COMPOSE"
    pw2 = d.textbbox((0, 0), powered, font=mono_small)[2]
    dot_x = (w - pw2) / 2 - 26
    d.ellipse((dot_x - 6, 1793, dot_x + 6, 1805), fill=CYAN + (255,))
    d.text(((w - pw2) / 2, 1783), powered, font=mono_small, fill=MUTED + (240,))

    ver_font = font("jetbrains_mono_regular.ttf", 25)
    ver = "v1.0.0"
    vw = d.textbbox((0, 0), ver, font=ver_font)[2]
    d.text(((w - vw) / 2, 1838), ver, font=ver_font, fill=(154, 163, 199, 150))

    out = DOCS / "splash_preview.png"
    base.convert("RGB").save(out, optimize=True)
    print(f"  docs/splash_preview.png  {out.stat().st_size / 1024:.0f} KB")


def build_banner():
    w, h = 1600, 760
    bg = Image.open(RES / "drawable-nodpi" / "splash_nebula.webp").convert("RGBA")
    # cover
    ratio = max(w / bg.width, h / bg.height)
    bg = bg.resize((int(bg.width * ratio), int(bg.height * ratio)), Image.LANCZOS)
    left = (bg.width - w) // 2
    top = (bg.height - h) // 2
    bg = bg.crop((left, top, left + w, top + h))

    overlay = Image.new("RGBA", (w, h))
    oarr = np.zeros((h, w, 4), dtype=np.uint8)
    oarr[..., 0], oarr[..., 1], oarr[..., 2] = DEEP_NAVY
    t = np.linspace(0, 1, w)[None, :].repeat(h, axis=0)
    oarr[..., 3] = (np.interp(t, [0, 0.55, 1], [225, 200, 120])).astype(np.uint8)
    bg.alpha_composite(Image.fromarray(oarr, "RGBA"))

    d = ImageDraw.Draw(bg)
    add_stars(d, (w, h), count=70, seed=9, scale=1.2)

    draw_logo(bg, 1230, 380, 430)

    title_font = font("almarai_extrabold.ttf", 104)
    title = "IDE ULTIMATE"
    # left-aligned gradient wordmark
    spacing = 6
    widths = [d.textbbox((0, 0), ch, font=title_font)[2] - d.textbbox((0, 0), ch, font=title_font)[0] for ch in title]
    layer = Image.new("RGBA", (w, 160), (0, 0, 0, 0))
    ld = ImageDraw.Draw(layer)
    x = 92
    for ch, wd in zip(title, widths):
        ld.text((x, 0), ch, font=title_font, fill=(255, 255, 255, 255))
        x += wd + spacing
    grad = linear_gradient_rgba((w, 160), None, None, [(255, 255, 255), LILAC, CYAN], horizontal=True)
    grad.putalpha(layer.getchannel("A"))
    bg.alpha_composite(grad, (0, 142))

    sub_font = font("almarai_regular.ttf", 44)
    sub = ar("بيئة تطوير متكاملة في راحة يدك")
    d.text((96, 318), sub, font=sub_font, fill=MUTED + (255,))

    chips = ["Kotlin", "Compose", "Splash", "RTL"]
    chip_font = font("jetbrains_mono_medium.ttf", 26)
    cx = 96
    for chip in chips:
        cw = d.textbbox((0, 0), chip, font=chip_font)[2] + 48
        d.rounded_rectangle((cx, 402, cx + cw, 462), radius=30, fill=(10, 16, 40, 165), outline=(167, 139, 250, 110), width=2)
        d.text((cx + 24, 416), chip, font=chip_font, fill=INK + (255,))
        cx += cw + 14

    foot_font = font("jetbrains_mono_regular.ttf", 24)
    d.text((96, 610), "github.com/AndroidEXL/IDE-Ultimate", font=foot_font, fill=(154, 163, 199, 210))

    out = DOCS / "banner.png"
    bg.convert("RGB").save(out, optimize=True)
    print(f"  docs/banner.png  {out.stat().st_size / 1024:.0f} KB")


def main():
    print("Generating IDE Ultimate assets…")
    build_nebula()
    build_icons()
    build_splash_preview()
    build_banner()
    print("Done.")


if __name__ == "__main__":
    main()
