# Release guide

How to produce the **32-bit**, **64-bit** and **universal** release APKs.

## 1. Create a release keystore (once)

```powershell
keytool -genkeypair -v -keystore C:\keys\chutur-paan-release.jks -alias chutur_paan -keyalg RSA -keysize 2048 -validity 10000
```

Back the `.jks` file up somewhere safe. If you lose it, you can never ship an update that installs over the old app.

Copy `keystore.properties.example` to `keystore.properties` in the project root and fill it in. It is git-ignored.

## 2. Gradle setup (`app/build.gradle.kts`)

Already done in the `app/build.gradle.kts` shipped with this repo:

- `keystore.properties` is read from the project root; if it exists, the `release` build is signed with
  **v1, v2 and v3** signatures. If it does not exist, the release APKs are built unsigned.
- `splits { abi { ... } }` builds a 32-bit (`armeabi-v7a`), a 64-bit (`arm64-v8a`) and a universal APK.

The script expects `versionCode = <number>` and `versionName = "<text>"` on single lines in `defaultConfig`.

## 3. Build

```powershell
powershell -ExecutionPolicy Bypass -File tools\build-release.ps1 -VersionName 1.1.0
```

Output in `dist\`:

| File | For |
|---|---|
| `ChuturPaan-<ver>-v7a-32bit.apk` | Older / 32-bit ARM phones |
| `ChuturPaan-<ver>-v8a-64bit.apk` | Modern 64-bit ARM phones (most phones today) |
| `ChuturPaan-<ver>-universal.apk` | Any phone (largest file) |
| `SHA256SUMS.txt` | Hashes to verify downloads |

### Universal APK vs. split APKs

Some dependencies (for example Firebase / Google Play services) ship native `.so` libraries, so the build produces three APKs.
The universal APK contains every ABI (`armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`) and works on any phone, but it is the largest.
The 32-bit and 64-bit APKs contain only their own ABI and are smaller. The script allows all four ABIs in the universal APK
and only the matching ABI in each split APK.

## 4. Publish on GitHub

1. Commit and push the code (never the keystore, `keystore.properties` or `google-services.json`).
2. Tag it: `git tag v1.1.0 && git push --tags`
3. GitHub, then Releases, then Draft a new release. Pick the tag and upload the files from `dist\` including `SHA256SUMS.txt`.

## Troubleshooting

| Problem | Fix |
|---|---|
| APK says UNSIGNED / "App not installed" | `keystore.properties` missing or `signingConfig` not wired in step 2 |
| `google-services.json` missing | Download from Firebase console into `app\` |
| Update won't install over old version | `versionCode` must be higher, and the same keystore must be used |
| apksigner not found warning | Set `ANDROID_HOME` to your Android SDK folder |
