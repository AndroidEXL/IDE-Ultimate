# Demo signing key

`ide-ultimate.p12` — **public demo key, committed on purpose.**

| Field | Value |
| --- | --- |
| Store type | `PKCS12` |
| File | `keystore/ide-ultimate.p12` |
| Alias | `ideultimate` |
| Store / key password | `ideultimate` |
| Certificate | `CN=IDE Ultimate Demo, O=IDE Ultimate, OU=Android, C=US` |
| Validity | 100 years |
| SHA-256 | `3D:4D:87:C0:3A:A7:74:C3:B3:E9:AF:FD:E2:5C:1C:D5:A3:1B:80:8C:80:BB:A8:6F:8F:5B:E8:16:4F:18:80:67` |

## Why commit a keystore?

GitHub Actions runners are fresh machines, so Android's auto-generated debug
keystore differs on every run. Each APK then has a different signature and
Android refuses to install it over a previous build ("App not installed").
Using this single key for **debug and release** makes every artifact
upgradeable and compatible with each other.

## This is not a secret

The key is published in this repository, so it provides **no security**. It
exists only so demo builds can be sideloaded and updated. Anyone can sign an
APK with it.

## Publishing to Google Play

Generate your own key, keep it out of Git, and pass it to Gradle:

```bash
./gradlew assembleRelease \
  -PIEU_STORE_FILE=/secure/path/upload-keystore.jks \
  -PIEU_STORE_PASSWORD=… \
  -PIEU_KEY_ALIAS=… \
  -PIEU_KEY_PASSWORD=…
```

The same values are read from the environment variables
`IEU_STORE_FILE`, `IEU_STORE_PASSWORD`, `IEU_KEY_ALIAS`, `IEU_KEY_PASSWORD`,
which is the recommended way to wire GitHub Actions secrets.
`app/build.gradle.kts` prefers this configuration automatically when
`IEU_STORE_FILE` is present.

Regenerate the demo key with:

```bash
python tools/create_signing_key.py
```
