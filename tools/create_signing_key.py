#!/usr/bin/env python3
"""
Creates the demo signing key used by every CI build.

Why this exists: GitHub Actions runners start from a clean image, so Android's
auto-generated debug keystore is different on every run. That gives each APK a
different signature, and Android then refuses to install a new build over the
old one ("App not installed"). Committing one keystore makes every artifact
upgradeable and keeps debug/release builds compatible with each other.

⚠️ This key is PUBLIC by design (it lives in the repository). It is only meant
for demo builds that people sideload. For Google Play, generate a private
keystore, keep it out of Git, and pass it through repository secrets —
`app/build.gradle.kts` already prefers the IDEU_* environment variables when
they are present.

Usage:
    python tools/create_signing_key.py
"""

from __future__ import annotations

import datetime as dt
from pathlib import Path

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives.serialization import pkcs12
from cryptography.x509.oid import NameOID

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "keystore" / "ide-ultimate.p12"

ALIAS = "ideultimate"
PASSWORD = "ideultimate"
VALIDITY_YEARS = 100


def main() -> None:
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)

    subject = issuer = x509.Name(
        [
            x509.NameAttribute(NameOID.COMMON_NAME, "IDE Ultimate Demo"),
            x509.NameAttribute(NameOID.ORGANIZATION_NAME, "IDE Ultimate"),
            x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, "Android"),
            x509.NameAttribute(NameOID.COUNTRY_NAME, "US"),
        ]
    )

    now = dt.datetime.now(dt.timezone.utc)
    certificate = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now - dt.timedelta(days=1))
        .not_valid_after(now + dt.timedelta(days=365 * VALIDITY_YEARS))
        .add_extension(x509.BasicConstraints(ca=False, path_length=None), critical=True)
        .add_extension(
            x509.KeyUsage(
                digital_signature=True,
                content_commitment=False,
                key_encipherment=False,
                data_encipherment=False,
                key_agreement=False,
                key_cert_sign=False,
                crl_sign=False,
                encipher_only=False,
                decipher_only=False,
            ),
            critical=True,
        )
        .sign(key, hashes.SHA256())
    )

    blob = pkcs12.serialize_key_and_certificates(
        name=ALIAS.encode(),
        key=key,
        cert=certificate,
        cas=None,
        encryption_algorithm=serialization.BestAvailableEncryption(PASSWORD.encode()),
    )

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_bytes(blob)

    fingerprint = certificate.fingerprint(hashes.SHA256()).hex().upper()
    print(f"Wrote {OUTPUT.relative_to(ROOT)} ({len(blob)} bytes)")
    print(f"  alias     : {ALIAS}")
    print(f"  password  : {PASSWORD}")
    print(f"  storeType : PKCS12")
    print(f"  SHA-256   : {':'.join(fingerprint[i:i + 2] for i in range(0, len(fingerprint), 2))}")


if __name__ == "__main__":
    main()
