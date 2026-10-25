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

Requirements:  pip install -r tools/requirements.txt
Usage:         python tools/generate_assets.py
"""

from __future__ import annotations

import math
import random
from pathlib import Path

import freetype
import numpy as np
import uharfbuzz as hb
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

    # Tagline (shaped with HarfBuzz)
    paste_text(base, "بيئة تطوير متكاملة… في راحة يدك", "almarai_regular.ttf", 42, w / 2, 1146, MUTED, align="center")

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
    # RTL layout: the Arabic status sits on the right, the percentage on the left.
    paste_text(base, "تجهيز المعاينة الفورية", "almarai_regular.ttf", 30, bar_x + bar_w, bar_y + 40, MUTED, align="right")
    paste_text(base, "62%", "jetbrains_mono_regular.ttf", 30, bar_x, bar_y + 44, CYAN, align="left")

    # Skip pill (top-left in RTL)
    skip_image, skip_width = shape_text("تخطي", "almarai_bold.ttf", 27, "rtl", INK)
    pill_w, pill_h = skip_width + 64, 68
    px, py = 66, 70
    d.rounded_rectangle((px, py, px + pill_w, py + pill_h), radius=pill_h // 2, fill=(255, 255, 255, 20), outline=(255, 255, 255, 45), width=2)
    paste_image(base, skip_image, (px + 32, py + 14))

    # Bottom branding
    mono_small = font("jetbrains_mono_medium.ttf", 27)
    powered = "KOTLIN  •  JETPACK COMPOSE"
    pw2 = d.textbbox((0, 0), powered, font=mono_small)[2]
    dot_x = (w - pw2) / 2 - 26
    d.ellipse((dot_x - 6, 1793, dot_x + 6, 1805), fill=CYAN + (255,))
    d.text(((w - pw2) / 2, 1783), powered, font=mono_small, fill=MUTED + (240,))

    ver_font = font("jetbrains_mono_regular.ttf", 25)
    ver = "v1.1.0"
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

    paste_text(bg, "بيئة تطوير متكاملة في راحة يدك", "almarai_regular.ttf", 44, 96, 312, MUTED, align="left")

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


# --------------------------------------------------------------------------- #
# Text rendering (HarfBuzz shaping + FreeType rasterisation)
# --------------------------------------------------------------------------- #
def shape_text(text, font_name, size, direction="rtl", color=INK, spacing=0.0):
    """Shape a run with HarfBuzz and rasterise it; returns (RGBA image, width)."""
    path = str(FONT_DIR / font_name)
    blob = hb.Blob.from_file_path(path)
    face = hb.Face(blob)
    hb_font = hb.Font(face)
    buffer = hb.Buffer()
    buffer.add_str(text)
    buffer.direction = direction
    buffer.script = "Arab" if direction == "rtl" else "Latn"
    buffer.language = "ar" if direction == "rtl" else "en"
    hb.shape(hb_font, buffer)

    upem = face.upem or 1000
    scale = size / upem
    ft = freetype.Face(path)
    ft.set_pixel_sizes(0, size)
    ascent = ft.size.ascender / 64.0

    pen_x = 0.0
    glyphs = []
    for info, pos in zip(buffer.glyph_infos, buffer.glyph_positions):
        glyphs.append((info.codepoint, pen_x + pos.x_offset * scale, pos.y_offset * scale))
        pen_x += pos.x_advance * scale + spacing

    width = max(1, int(math.ceil(pen_x)) + 8)
    height = max(1, int(math.ceil(ft.size.height / 64.0)) + 10)
    mask = Image.new("L", (width, height), 0)
    for gid, gx, gy in glyphs:
        ft.load_glyph(gid, freetype.FT_LOAD_RENDER)
        bitmap = ft.glyph.bitmap
        if bitmap.width == 0 or bitmap.rows == 0:
            continue
        pitch = abs(bitmap.pitch)
        raw = bytes(bitmap.buffer)
        rows = [raw[r * pitch:r * pitch + bitmap.width] for r in range(bitmap.rows)]
        glyph_img = Image.frombytes("L", (bitmap.width, bitmap.rows), b"".join(rows))
        left = int(round(gx + ft.glyph.bitmap_left))
        top = int(round(ascent - gy - ft.glyph.bitmap_top))
        mask.paste(255, (left, top, left + bitmap.width, top + bitmap.rows), glyph_img)

    image = Image.new("RGBA", (width, height), color + (0,))
    image.putalpha(mask)
    return image, int(round(pen_x))


def with_alpha(image, alpha):
    out = image.copy()
    out.putalpha(out.getchannel("A").point(lambda v: int(v * alpha)))
    return out


def _is_rtl_char(ch):
    return "\u0590" <= ch <= "\u08FF" or "\uFB1D" <= ch <= "\uFDFF" or "\uFE70" <= ch <= "\uFEFF"


def bidi_runs(text):
    """Split a paragraph into (run_text, is_rtl) runs, preserving spaces."""
    runs = []
    for ch in text:
        rtl = _is_rtl_char(ch)
        if runs and runs[-1][1] == rtl:
            runs[-1][0].append(ch)
        else:
            runs.append([[ch], rtl])
    return [("".join(chars), rtl) for chars, rtl in runs]


def layout_paragraph(text, font_name, size, color=INK, max_width=None):
    """Bidi-aware text layout: shapes each run and orders runs for the base direction."""
    base_rtl = any(_is_rtl_char(ch) for ch in text)
    runs = bidi_runs(text.replace("\n", " "))

    def build(font_size):
        shaped = []
        for run_text, rtl in runs:
            image, width = shape_text(
                run_text, font_name, font_size, "rtl" if rtl else "ltr", color
            )
            shaped.append((image, width, rtl))
        return shaped, sum(item[1] for item in shaped)

    shaped, total = build(size)
    if max_width is not None and total > max_width:
        # Shrink a little first, then fall back to an ellipsis.
        shrunk = size
        while total > max_width and shrunk > int(size * 0.72):
            shrunk -= 1
            shaped, total = build(shrunk)
        if total > max_width:
            budget = max_width - int(shrunk * 0.55)
            clipped, used = [], 0
            for image, width, rtl in shaped:
                if used + width <= budget:
                    clipped.append((image, width, rtl))
                    used += width
                elif image is not None:
                    clipped.append((image, width, rtl))
                    used += width
                    break
            ellipsis, ell_width = shape_text("…", font_name, shrunk,
                                             "rtl" if base_rtl else "ltr", color)
            clipped.append((ellipsis, ell_width, base_rtl))
            shaped, total = clipped, sum(item[1] for item in clipped)

    height = max([img.height for img, _, _ in shaped if img is not None] + [int(size * 1.4)]) + 8
    canvas = Image.new("RGBA", (max(1, total + 8), height), (0, 0, 0, 0))

    if base_rtl:
        x = total + 4
        for image, width, _ in reversed(shaped):
            x -= width
            if image is not None:
                canvas.alpha_composite(image, (int(x), 0))
    else:
        x = 4
        for image, width, _ in shaped:
            if image is not None:
                canvas.alpha_composite(image, (int(x), 0))
            x += width
    return canvas, total


def paste_image(canvas, image, xy, alpha=1.0):
    if alpha < 1.0:
        image = with_alpha(image, alpha)
    canvas.alpha_composite(image, (int(xy[0]), int(xy[1])))


def paste_text(canvas, text, font_name, size, x, y, color=INK, align="left",
               alpha=1.0, max_width=None):
    """Paste bidi-aware text on a canvas. `align` accepts left/right/center."""
    image, width = layout_paragraph(text, font_name, size, color, max_width)
    if align == "right":
        pos_x = x - width
    elif align == "center":
        pos_x = x - width / 2
    else:
        pos_x = x
    paste_image(canvas, image, (pos_x, y), alpha)
    return width


# --------------------------------------------------------------------------- #
# Workspace screen previews
# --------------------------------------------------------------------------- #
def glass_panel(canvas, box, radius, accent=None, fill_top=31, fill_bottom=8):
    x0, y0, x1, y1 = [int(v) for v in box]
    w, h = x1 - x0, y1 - y0
    arr = np.zeros((h, w, 4), dtype=np.uint8)
    t = np.linspace(0, 1, h)[:, None].repeat(w, axis=1)
    arr[..., 0:3] = 255
    arr[..., 3] = (fill_top + (fill_bottom - fill_top) * t).astype(np.uint8)
    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    layer.paste(Image.fromarray(arr, "RGBA"), (0, 0), rounded_mask((w, h), radius))

    if accent:
        gradient = linear_gradient_rgba((w, 2), None, None, [accent, accent])
        line = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        ImageDraw.Draw(line).rounded_rectangle((0, 0, w - 1, h - 1), radius=radius,
                                               outline=accent + (140,), width=2)
        line = Image.composite(line, Image.new("RGBA", (w, h), (0, 0, 0, 0)), rounded_mask((w, h), radius))
        layer.alpha_composite(line)
    else:
        line = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        ImageDraw.Draw(line).rounded_rectangle((0, 0, w - 1, h - 1), radius=radius,
                                               outline=(255, 255, 255, 30), width=2)
        layer.alpha_composite(line)
    canvas.alpha_composite(layer, (x0, y0))


def draw_folder_glyph(canvas, box, accent):
    x0, y0, x1, y1 = [int(v) for v in box]
    w, h = x1 - x0, y1 - y0
    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    radius = int(w * 0.11)

    tab = (int(w * 0.07), int(h * 0.12), int(w * 0.47), int(h * 0.42))
    d.rounded_rectangle(tab, radius=int(w * 0.07), fill=accent + (105,))

    body = (int(w * 0.04), int(h * 0.30), int(w * 0.96), int(h * 0.92))
    bw, bh = body[2] - body[0], body[3] - body[1]
    gradient = linear_gradient_rgba((bw, bh), None, None, [accent, tuple(int(c * 0.55) for c in accent)])
    gradient.putalpha(gradient.getchannel("A").point(lambda v: int(v * 0.72)))
    body_layer = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    body_layer.paste(gradient, (0, 0), rounded_mask((bw, bh), radius))
    layer.alpha_composite(body_layer, (body[0], body[1]))

    outline = Image.new("RGBA", (bw, bh), (0, 0, 0, 0))
    ImageDraw.Draw(outline).rounded_rectangle((0, 0, bw - 1, bh - 1), radius=radius,
                                              outline=accent + (150,), width=max(2, int(w * 0.018)))
    layer.alpha_composite(outline, (body[0], body[1]))

    shine = (body[0] + int(w * 0.07), body[1] + int(h * 0.08),
             body[0] + int(w * 0.57), body[1] + int(h * 0.11))
    d.rounded_rectangle(shine, radius=shine[3] - shine[1], fill=(255, 255, 255, 55))
    canvas.alpha_composite(layer, (x0, y0))


def draw_code_panel(canvas, box, accent_lines):
    x0, y0, x1, y1 = [int(v) for v in box]
    w, h = x1 - x0, y1 - y0
    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.rounded_rectangle((0, 0, w - 1, h - 1), radius=int(w * 0.09), fill=(7, 11, 28, 205),
                        outline=(255, 255, 255, 26), width=2)
    dot_r = max(4, int(w * 0.013))
    for i, color in enumerate([(255, 95, 87), (254, 188, 46), (40, 200, 64)]):
        cx = int(w * 0.08) + i * int(w * 0.045)
        d.ellipse((cx - dot_r, int(h * 0.12) - dot_r, cx + dot_r, int(h * 0.12) + dot_r), fill=color + (255,))
    line_y = int(h * 0.33)
    for fraction, color in accent_lines:
        d.rounded_rectangle(
            (int(w * 0.09), line_y, int(w * 0.09) + int(w * 0.82 * fraction), line_y + max(4, int(h * 0.035))),
            radius=max(2, int(h * 0.02)),
            fill=color + (190,),
        )
        line_y += int(h * 0.15)
    canvas.alpha_composite(layer, (x0, y0))


def draw_fab(canvas, cx, cy, size):
    canvas.alpha_composite(radial_rgba(canvas.size, (cx, cy), size * 1.05, VIOLET, 120, 2.0))
    s = int(size)
    gradient = linear_gradient_rgba((s, s), None, None, [VIOLET, (139, 92, 246), (217, 70, 239)])
    fab = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    fab.paste(gradient, (0, 0), rounded_mask((s, s), int(s * 0.35)))
    fab.alpha_composite(Image.new("RGBA", (s, s), (0, 0, 0, 0)))
    d = ImageDraw.Draw(fab)
    d.rounded_rectangle((1, 1, s - 2, s - 2), radius=int(s * 0.35), outline=(255, 255, 255, 80), width=2)
    plus = int(s * 0.10)
    d.rounded_rectangle((s / 2 - s * 0.22, s / 2 - plus / 2, s / 2 + s * 0.22, s / 2 + plus / 2),
                        radius=plus, fill=(255, 255, 255, 255))
    d.rounded_rectangle((s / 2 - plus / 2, s / 2 - s * 0.22, s / 2 + plus / 2, s / 2 + s * 0.22),
                        radius=plus, fill=(255, 255, 255, 255))
    canvas.alpha_composite(fab, (int(cx - s / 2), int(cy - s / 2)))



def draw_bottom_bar(canvas, selected="workspace"):
    """Floating two-tab glass bar mirroring AppBottomBar (RTL order)."""
    w = canvas.width
    bar_w, bar_h = w - 100, 128
    x0 = (w - bar_w) // 2
    y0 = canvas.height - bar_h - 46

    layer = Image.new("RGBA", (bar_w, bar_h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.rounded_rectangle((0, 0, bar_w - 1, bar_h - 1), radius=48, fill=(12, 18, 48, 242),
                        outline=(255, 255, 255, 38), width=2)

    item_w = (bar_w - 24 - 12) // 2
    # RTL layout: the first tab (Home) sits on the right.
    for index, (label, is_selected) in enumerate([("الرئيسية", selected == "home"),
                                                  ("مساحة العمل", selected == "workspace")]):
        ix = bar_w - 12 - item_w - index * (item_w + 12)
        iy = 12
        if is_selected:
            grad = linear_gradient_rgba((item_w, bar_h - 24), None, None, [(90, 42, 170), (124, 58, 237)])
            pill = Image.new("RGBA", (item_w, bar_h - 24), (0, 0, 0, 0))
            pill.paste(grad, (0, 0), rounded_mask((item_w, bar_h - 24), 38))
            pill.putalpha(pill.getchannel("A").point(lambda v: int(v * 0.9)))
            layer.alpha_composite(pill, (ix, iy))
            d.rounded_rectangle((ix, iy, ix + item_w - 1, iy + bar_h - 25), radius=38,
                                outline=(167, 139, 250, 130), width=2)

        center_x = ix + item_w // 2
        icon_y = iy + 34
        if index == 0:
            d.polygon([(center_x - 17, icon_y), (center_x, icon_y - 15), (center_x + 17, icon_y)],
                      outline=(255, 255, 255, 230), width=4)
            d.rectangle((center_x - 12, icon_y, center_x + 12, icon_y + 16),
                        outline=(255, 255, 255, 230), width=4)
        else:
            d.rounded_rectangle((center_x - 17, icon_y - 14, center_x - 2, icon_y - 2), radius=4,
                                fill=(255, 255, 255, 230))
            d.rounded_rectangle((center_x - 19, icon_y - 4, center_x + 19, icon_y + 15), radius=5,
                                fill=(255, 255, 255, 230))

        text_color = (255, 255, 255) if is_selected else MUTED
        paste_text(layer, label, "almarai_bold.ttf", 20, center_x, iy + 62, text_color, align="center")

    canvas.alpha_composite(layer, (x0, y0))

def workspace_base(title, subtitle):
    w, h = 1080, 1920
    base = Image.open(RES / "drawable-nodpi" / "splash_nebula.webp").convert("RGBA").resize((w, h), Image.LANCZOS)

    arr = np.zeros((h, w, 4), dtype=np.uint8)
    ys = np.linspace(0, 1, h)
    stops = [(0.0, 0.62), (0.5, 0.92), (1.0, 1.0)]
    alphas = np.interp(ys, [s[0] for s in stops], [s[1] for s in stops]) * 255
    arr[..., 0], arr[..., 1], arr[..., 2] = DEEP_NAVY
    arr[..., 3] = alphas[:, None].astype(np.uint8)
    base.alpha_composite(Image.fromarray(arr, "RGBA"))

    d = ImageDraw.Draw(base)
    add_stars(d, (w, h), count=42, seed=27, scale=1.0)

    # Back button on the right (RTL) + logo on the left.
    d.ellipse((920, 92, 1020, 192), fill=(255, 255, 255, 22), outline=(255, 255, 255, 40), width=2)
    d.line([(992, 128), (968, 144), (992, 160)], fill=INK + (255,), width=6, joint="curve")
    draw_logo(base, 86, 144, 108)

    paste_text(base, title, "almarai_bold.ttf", 44, 892, 66, INK, align="right")
    paste_text(base, subtitle, "almarai_regular.ttf", 26, 892, 126, MUTED, align="right")

    # Breadcrumbs (drawn on a layer so alpha is composited).
    x = 1010
    for label, selected in [("قوالب جاهزة", True), ("الجذر", False)]:
        image, width = layout_paragraph(label, "almarai_regular.ttf", 26, CYAN if selected else MUTED)
        chip_w = width + 44
        fill = (103, 232, 249, 38) if selected else (255, 255, 255, 14)
        outline = (103, 232, 249, 110) if selected else (255, 255, 255, 36)
        chip = Image.new("RGBA", (chip_w, 56), (0, 0, 0, 0))
        ImageDraw.Draw(chip).rounded_rectangle((1, 1, chip_w - 2, 54), radius=28,
                                               fill=fill, outline=outline, width=2)
        chip.alpha_composite(image, (22, 12))
        base.alpha_composite(chip, (int(x - chip_w), 196))
        x -= chip_w + 10

    draw_bottom_bar(base, selected="workspace")
    return base


def build_workspace_preview():
    base = workspace_base("مساحة العمل", "نظّم مشاريعك في مجلدات")
    d = ImageDraw.Draw(base)

    card_w, gap, left, top = 476, 28, 50, 280
    cards = [
        ("مشاريع Android", "العناصر: 2", CYAN, "folder"),
        ("قوالب جاهزة", "العناصر: 1", LILAC, "folder"),
        ("HelloCompose", "مشروع Kotlin", CYAN, "project"),
        ("Playground", "مشروع Kotlin", VIOLET_SOFT, "project"),
    ]

    for index, (name, meta, accent, kind) in enumerate(cards):
        row, col = divmod(index, 2)
        x0 = left + (1 - col) * (card_w + gap)  # RTL: first column on the right
        y0 = top + row * (card_w + gap)
        glass_panel(base, (x0, y0, x0 + card_w, y0 + card_w), 62, accent)
        if kind == "folder":
            draw_folder_glyph(base, (x0 + 34, y0 + 40, x0 + card_w - 34, y0 + card_w * 0.62), accent)
        else:
            draw_code_panel(
                base,
                (x0 + 22, y0 + 22, x0 + card_w - 22, y0 + card_w * 0.66),
                [(0.78, FUCHSIA), (0.52, CYAN), (0.88, VIOLET_SOFT), (0.38, TEAL)],
            )
        paste_text(base, name, "almarai_bold.ttf", 34, x0 + card_w - 28, y0 + card_w - 126, INK,
                   align="right", max_width=card_w - 56)
        paste_text(base, meta, "almarai_regular.ttf", 26, x0 + card_w - 28, y0 + card_w - 78, MUTED,
                   align="right", max_width=card_w - 56)

    draw_fab(base, 148, 1666, 150)

    out = DOCS / "workspace_preview.png"
    base.convert("RGB").save(out, optimize=True)
    print(f"  docs/workspace_preview.png  {out.stat().st_size / 1024:.0f} KB")


def draw_action_row(canvas, y, title, description, tint, destructive):
    w = 1080
    box = (60, y, w - 60, y + 132)
    fill = (255, 92, 92, 26) if destructive else (255, 255, 255, 6)
    outline = (255, 92, 92, 60) if destructive else (255, 255, 255, 26)
    glass_panel(canvas, box, 44, None, fill_top=fill[3], fill_bottom=fill[3])
    d = ImageDraw.Draw(canvas)
    d.rounded_rectangle(box, radius=44, outline=outline, width=2)

    icon_box = (w - 60 - 116, y + 26, w - 60 - 26, y + 116)
    d.rounded_rectangle(icon_box, radius=34, fill=tint + (34,), outline=tint + (70,), width=2)
    cx = (icon_box[0] + icon_box[2]) / 2
    cy = (icon_box[1] + icon_box[3]) / 2
    ink = (255, 138, 138) if destructive else tint

    if destructive:
        d.rounded_rectangle((cx - 18, cy - 8, cx + 18, cy + 22), radius=5, outline=ink + (255,), width=5)
        d.line((cx - 25, cy - 18, cx + 25, cy - 18), fill=ink + (255,), width=5)
        d.line((cx - 9, cy - 27, cx + 9, cy - 27), fill=ink + (255,), width=5)
    elif title.startswith("تعديل"):
        d.line((cx + 15, cy - 20, cx - 7, cy + 6), fill=ink + (255,), width=6)
        d.polygon([(cx - 16, cy + 14), (cx - 3, cy + 9), (cx - 11, cy)], fill=ink + (255,))
    elif title.startswith("تصدير"):
        d.line((cx - 16, cy - 18, cx + 16, cy - 18), fill=ink + (255,), width=6)
        d.line((cx + 16, cy - 18, cx + 16, cy + 18), fill=ink + (255,), width=6)
        d.line((cx + 16, cy + 18, cx - 16, cy + 18), fill=ink + (255,), width=6)
        d.line((cx - 16, cy + 18, cx - 16, cy - 18), fill=ink + (255,), width=6)
        d.line((cx, cy - 4, cx, cy + 16), fill=ink + (255,), width=6)
        d.line((cx - 12, cy + 4, cx, cy + 18), fill=ink + (255,), width=6)
        d.line((cx + 12, cy + 4, cx, cy + 18), fill=ink + (255,), width=6)
    else:
        d.line((cx - 20, cy, cx + 20, cy), fill=ink + (255,), width=7)
        d.line((cx, cy - 20, cx, cy + 20), fill=ink + (255,), width=7)

    text_right = icon_box[0] - 28
    paste_text(canvas, title, "almarai_bold.ttf", 34, text_right, y + 28,
               (255, 155, 155) if destructive else INK, align="right")
    paste_text(canvas, description, "almarai_regular.ttf", 26, text_right, y + 80, MUTED, align="right")


def build_workspace_sheet_preview():
    base = workspace_base("مساحة العمل", "نظّم مشاريعك في مجلدات")

    card_w, gap, left, top = 476, 28, 50, 280
    for index, accent in enumerate([CYAN, LILAC]):
        x0 = left + (1 - index) * (card_w + gap)
        glass_panel(base, (x0, top, x0 + card_w, top + card_w), 62, accent)
        draw_folder_glyph(base, (x0 + 34, top + 40, x0 + card_w - 34, top + card_w * 0.62), accent)

    base.alpha_composite(Image.new("RGBA", (1080, 1920), (3, 6, 15, 205)))

    sheet_top = 966
    sheet = Image.new("RGBA", (1080, 1920), (0, 0, 0, 0))
    ImageDraw.Draw(sheet).rounded_rectangle((0, sheet_top, 1080, 1960), radius=74, fill=(11, 16, 40, 255))
    base.alpha_composite(sheet)
    d = ImageDraw.Draw(base)
    d.rounded_rectangle((480, sheet_top + 26, 600, sheet_top + 36), radius=5, fill=(255, 255, 255, 64))

    paste_text(base, "مشروع Kotlin", "almarai_bold.ttf", 24, 1020, sheet_top + 62, CYAN, align="right")
    paste_text(base, "NotesApp", "almarai_bold.ttf", 46, 1020, sheet_top + 100, INK, align="right")
    paste_text(base, "خيارات المشروع", "almarai_regular.ttf", 28, 1020, sheet_top + 174, MUTED, align="right")

    actions = [
        ("تعديل الاسم", "غيّر اسم العنصر في أي وقت.", CYAN, False),
        ("تصدير الكود (ZIP)", "نزّل ملف ZIP يحتوي مشروع Gradle جاهزًا.", VIOLET_SOFT, False),
        ("إنشاء مشروع جديد", "أضف مشروعًا جديدًا في نفس المجلد.", TEAL, False),
        ("حذف", "حذف المشروع من مساحة العمل.", (255, 107, 107), True),
    ]
    y = sheet_top + 246
    for title, desc, tint, destructive in actions:
        draw_action_row(base, y, title, desc, tint, destructive)
        y += 150

    out = DOCS / "workspace_sheet_preview.png"
    base.convert("RGB").save(out, optimize=True)
    print(f"  docs/workspace_sheet_preview.png  {out.stat().st_size / 1024:.0f} KB")


def build_project_detail_preview():
    base = workspace_base("NotesApp", "مشروع Kotlin")

    # Project hero card.
    card = (48, 300, 1032, 900)
    glass_panel(base, card, 60, CYAN)
    d = ImageDraw.Draw(base)
    d.rounded_rectangle((914, 336, 1000, 422), radius=28, fill=(255, 255, 255, 12),
                        outline=CYAN + (90,), width=2)
    paste_text(base, "</>", "jetbrains_mono_bold.ttf", 26, 957, 366, CYAN, align="center")

    paste_text(base, "NotesApp", "almarai_bold.ttf", 44, 884, 342, INK, align="right")
    paste_text(base, "جاهز للتصدير والبناء", "almarai_regular.ttf", 26, 884, 404, TEAL, align="right")

    paste_text(base, "اسم الحزمة (applicationId)", "almarai_bold.ttf", 24, 960, 460, MUTED, align="right")
    pkg = Image.new("RGBA", (840, 54), (0, 0, 0, 0))
    ImageDraw.Draw(pkg).rounded_rectangle((0, 0, 839, 53), radius=14, fill=(7, 11, 28, 140))
    base.alpha_composite(pkg, (96, 500))
    paste_text(base, "com.ideultimate.projects.notesapp", "jetbrains_mono_medium.ttf", 26, 870, 510,
               CYAN, align="right")

    # Primary + icon buttons row.
    grad = linear_gradient_rgba((560, 92), None, None, [VIOLET, (139, 92, 246), (217, 70, 239)])
    btn = Image.new("RGBA", (560, 92), (0, 0, 0, 0))
    btn.paste(grad, (0, 0), rounded_mask((560, 92), 30))
    base.alpha_composite(btn, (470, 596))
    paste_text(base, "تصدير ZIP", "almarai_bold.ttf", 30, 750, 618, (255, 255, 255), align="center")

    icons_layer = Image.new("RGBA", (1080, 1920), (0, 0, 0, 0))
    icons_layer_draw = ImageDraw.Draw(icons_layer)
    for index, cx in enumerate([400, 320, 240]):
        icons_layer_draw.ellipse((cx - 44, 620, cx + 44, 708), fill=(255, 255, 255, 17),
                                 outline=(255, 255, 255, 40), width=2)
        icon_center = (cx, 664)
        if index == 0:
            icons_layer_draw.line((cx - 16, 646, cx + 16, 646), fill=INK + (255,), width=6)
            icons_layer_draw.line((cx + 16, 646, cx + 16, 682), fill=INK + (255,), width=6)
            icons_layer_draw.line((cx + 16, 682, cx - 16, 682), fill=INK + (255,), width=6)
            icons_layer_draw.line((cx - 16, 682, cx - 16, 646), fill=INK + (255,), width=6)
            icons_layer_draw.line((cx, 660, cx, 680), fill=INK + (255,), width=6)
        elif index == 1:
            icons_layer_draw.line((cx + 14, 646, cx - 6, 670), fill=INK + (255,), width=6)
            icons_layer_draw.polygon([(cx - 14, 678), (cx - 2, 673), (cx - 10, 664)], fill=INK + (255,))
        else:
            icons_layer_draw.rounded_rectangle((cx - 16, 654, cx + 16, 682), radius=5,
                                               outline=INK + (255,), width=5)
            icons_layer_draw.line((cx - 22, 646, cx + 22, 646), fill=INK + (255,), width=5)
            icons_layer_draw.line((cx - 8, 638, cx + 8, 638), fill=INK + (255,), width=5)
    base.alpha_composite(icons_layer)

    paste_text(base, "سيُصدَّر مشروع Gradle كامل يمكن فتحه مباشرة في Android Studio.",
               "almarai_regular.ttf", 22, 960, 742, MUTED, align="right", max_width=860)

    # Files section.
    heading_w = paste_text(base, "ملفات المشروع", "almarai_bold.ttf", 38, 1000, 950, INK, align="right")
    chip = Image.new("RGBA", (64, 44), (0, 0, 0, 0))
    ImageDraw.Draw(chip).rounded_rectangle((0, 0, 63, 43), radius=22, fill=CYAN + (30,), outline=CYAN + (80,), width=2)
    chip_x = 1000 - heading_w - 22 - 64
    base.alpha_composite(chip, (chip_x, 952))
    paste_text(base, "7", "jetbrains_mono_medium.ttf", 24, chip_x + 32, 959, CYAN, align="center")
    paste_text(base, "هذه الملفات تُضمَّن داخل ملف الـ ZIP.", "almarai_regular.ttf", 24, 1000, 1010, MUTED, align="right")

    files = [
        ("إعدادات المشروع", "settings.gradle.kts", "KTS", CYAN),
        ("إعدادات البناء الرئيسية", "build.gradle.kts", "KTS", VIOLET_SOFT),
        ("إعدادات بناء التطبيق", "app/build.gradle.kts", "KTS", FUCHSIA),
        ("بيان التطبيق", "app/src/main/AndroidManifest.xml", "XML", VIOLET_SOFT),
        ("الشاشة الرئيسية", "app/src/main/java/.../MainActivity.kt", "KT", TEAL),
    ]

    y = 1070
    for label, path, badge, tint in files:
        glass_panel(base, (48, y, 1032, y + 116), 32, None)
        d.rounded_rectangle((938, y + 32, 1000, y + 84), radius=16, fill=tint + (34,), outline=tint + (70,), width=2)
        paste_text(base, badge, "jetbrains_mono_bold.ttf", 18, 969, y + 48, tint, align="center")
        paste_text(base, label, "almarai_bold.ttf", 30, 910, y + 22, INK, align="right")
        paste_text(base, path, "jetbrains_mono_regular.ttf", 18, 910, y + 68, MUTED, align="right")
        y += 128

    draw_fab(base, 0, 0, 0) if False else None

    out = DOCS / "project_detail_preview.png"
    base.convert("RGB").save(out, optimize=True)
    print(f"  docs/project_detail_preview.png  {out.stat().st_size / 1024:.0f} KB")

def main():
    print("Generating IDE Ultimate assets…")
    build_nebula()
    build_icons()
    build_splash_preview()
    build_banner()
    build_workspace_preview()
    build_workspace_sheet_preview()
    build_project_detail_preview()
    print("Done.")


if __name__ == "__main__":
    main()
