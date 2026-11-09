<#
.SYNOPSIS
  Builds the Chutur Paan release APKs (32-bit, 64-bit and universal), verifies each one,
  and copies them to .\dist with clear names plus a SHA256SUMS.txt.

.DESCRIPTION
  One Gradle run produces all APKs (ABI splits, see docs\RELEASE.md). This script then:
    - reads versionName / versionCode from app\build.gradle.kts
    - optionally bumps them when a NEW version name is passed with -VersionName
      (versionCode is incremented by 1 automatically)
    - deletes old APKs so a stale file can never be shipped
    - opens every APK as a zip and checks it has classes.dex, a manifest, and only the
      native-library ABI folders it is supposed to have
    - verifies the signature with apksigner (when the Android SDK is found)
    - copies the result to .\dist\ChuturPaan-<version>-<variant>.apk and writes SHA-256 hashes

  NOTE: the universal APK contains every ABI that any dependency ships (armeabi-v7a, arm64-v8a,
  x86, x86_64), so it is allowed to have all four lib\ folders. The 32-bit and 64-bit APKs may
  only contain their own ABI. If no native libraries end up in the build at all, Gradle only
  produces the universal APK and the script reports that instead of failing.

  Prerequisites (once):
    1. Release keystore + keystore.properties in the project root (docs\RELEASE.md).
       Without them the APKs are UNSIGNED and Android will refuse to install them.
    2. app\google-services.json (Firebase push), not committed to git.

  Run from the project root:
    powershell -ExecutionPolicy Bypass -File tools\build-release.ps1
    powershell -ExecutionPolicy Bypass -File tools\build-release.ps1 -VersionName 1.2.0   # bumps versionName and versionCode (+1)
    powershell -ExecutionPolicy Bypass -File tools\build-release.ps1 -Clean
    powershell -ExecutionPolicy Bypass -File tools\build-release.ps1 -VersionName 1.2.0 -Clean
#>
param(
    [string]$VersionName,   # e.g. 1.2.0  (omit to keep the current version; versionCode is read from the project)
    [switch]$Clean          # run "gradlew clean" first
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$appName    = "ChuturPaan"
$gradleFile = Join-Path $root "app\build.gradle.kts"

# --- 1. inputs present?
if (-not (Test-Path (Join-Path $root "gradlew.bat"))) { throw "gradlew.bat not found. Run this script from the project (tools\ folder inside the repo)." }
if (-not (Test-Path $gradleFile))                      { throw "app\build.gradle.kts not found." }
if (-not (Test-Path (Join-Path $root "app\google-services.json"))) {
    throw "app\google-services.json is missing (Firebase config). Download it from the Firebase console and put it in app\."
}
$signed = Test-Path (Join-Path $root "keystore.properties")
if (-not $signed) { Write-Warning "keystore.properties not found: the APKs will be UNSIGNED and cannot be installed. See docs\RELEASE.md." }

# --- 2. read version from the project; optional bump
$gradleText = [IO.File]::ReadAllText($gradleFile)
$codeMatch  = [regex]::Match($gradleText, 'versionCode\s*=\s*(\d+)')
$nameMatch  = [regex]::Match($gradleText, 'versionName\s*=\s*"([^"]+)"')
if (-not $codeMatch.Success) { throw "Could not find versionCode in app\build.gradle.kts." }
if (-not $nameMatch.Success) { throw "Could not find versionName in app\build.gradle.kts." }

$currentCode = [int]$codeMatch.Groups[1].Value
$currentName = $nameMatch.Groups[1].Value
$requested   = $VersionName

# Only bump when a *different* version name is requested
if ($requested -and $requested -ne $currentName) {
    $newCode = $currentCode + 1
    $gradleText = [regex]::Replace($gradleText, '(versionName\s*=\s*")[^"]+(")', { param($m) $m.Groups[1].Value + $requested + $m.Groups[2].Value })
    $gradleText = [regex]::Replace($gradleText, '(versionCode\s*=\s*)\d+',       { param($m) $m.Groups[1].Value + $newCode })
    [IO.File]::WriteAllText($gradleFile, $gradleText, (New-Object Text.UTF8Encoding $false))
    $currentCode = $newCode
    $currentName = $requested
    Write-Host "Version bumped: versionCode = $currentCode, versionName = $currentName" -ForegroundColor Cyan
}
$buildVersion = $currentName
Write-Host "Building $appName $buildVersion (versionCode $currentCode)" -ForegroundColor Cyan

# --- 3. build (old APKs removed first so nothing stale can slip into dist)
$apkDir = Join-Path $root "app\build\outputs\apk\release"
if (Test-Path $apkDir) { Get-ChildItem $apkDir -Filter *.apk -File | Remove-Item -Force }
if ($Clean) { & (Join-Path $root "gradlew.bat") clean; if ($LASTEXITCODE -ne 0) { throw "Gradle clean failed." } }
& (Join-Path $root "gradlew.bat") assembleRelease
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed." }

# --- 4. locate apksigner (optional)
$apksigner = $null
foreach ($sdk in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA "Android\Sdk"))) {
    if ($sdk -and (Test-Path (Join-Path $sdk "build-tools"))) {
        $apksigner = Get-ChildItem (Join-Path $sdk "build-tools") -Directory | Sort-Object { [version]($_.Name -replace '[^\d\.].*$','') } -Descending |
            ForEach-Object { Join-Path $_.FullName "apksigner.bat" } | Where-Object { Test-Path $_ } | Select-Object -First 1
        if ($apksigner) { break }
    }
}
if (-not $apksigner) { Write-Warning "apksigner not found (set ANDROID_HOME). Signature will not be double-checked." }

# --- 5. inspect each APK
function Get-ApkInfo([string]$path) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($path)
    try {
        $names = @($zip.Entries | ForEach-Object { $_.FullName })
        [pscustomobject]@{
            HasDex      = [bool]($names -contains "classes.dex")
            HasManifest = [bool]($names -contains "AndroidManifest.xml")
            LibAbis     = @($names | Where-Object { $_ -like "lib/*/*" } | ForEach-Object { $_.Split("/")[1] } | Select-Object -Unique)
        }
    } finally { $zip.Dispose() }
}

# The universal APK holds every ABI any dependency ships (x86 / x86_64 included).
# The split APKs may only hold their own ABI.
$plan = @(
    @{ Pattern = "*universal*.apk";   Name = "universal";  Abis = @("armeabi-v7a", "arm64-v8a", "x86", "x86_64") },
    @{ Pattern = "*armeabi-v7a*.apk"; Name = "v7a-32bit";  Abis = @("armeabi-v7a") },
    @{ Pattern = "*arm64-v8a*.apk";   Name = "v8a-64bit";  Abis = @("arm64-v8a") }
)

$dist = Join-Path $root "dist"
if (Test-Path $dist) { Get-ChildItem $dist -Filter "$appName-*.apk" -File | Remove-Item -Force }
New-Item -ItemType Directory -Force $dist | Out-Null

$failed = $false
$sums = New-Object System.Collections.Generic.List[string]
$universalHasNative = $false

foreach ($p in $plan) {
    $apk = Get-ChildItem $apkDir -Filter $p.Pattern -File -ErrorAction SilentlyContinue | Select-Object -First 1

    if (-not $apk) {
        if ($p.Name -ne "universal" -and -not $universalHasNative) {
            Write-Host "[$($p.Name)] not generated: the app has no native libraries, so it would be identical to the universal APK." -ForegroundColor Yellow
        } else {
            Write-Host "[$($p.Name)] NOT FOUND in $apkDir" -ForegroundColor Red; $failed = $true
        }
        continue
    }

    $info = Get-ApkInfo $apk.FullName
    $ok = $true
    if (-not $info.HasDex)      { Write-Host "[$($p.Name)] classes.dex missing" -ForegroundColor Red; $ok = $false }
    if (-not $info.HasManifest) { Write-Host "[$($p.Name)] AndroidManifest.xml missing" -ForegroundColor Red; $ok = $false }
    foreach ($abi in $info.LibAbis) {
        if ($p.Abis -notcontains $abi) { Write-Host "[$($p.Name)] unexpected native ABI folder lib/$abi" -ForegroundColor Red; $ok = $false }
    }
    if ($p.Name -eq "universal") { $universalHasNative = ($info.LibAbis.Count -gt 0) }
    elseif ($info.LibAbis.Count -eq 0) { Write-Host "[$($p.Name)] contains no native libraries" -ForegroundColor Yellow }

    $isUnsigned = $apk.Name -like "*unsigned*"
    if (-not $isUnsigned -and $apksigner) {
        & $apksigner verify $apk.FullName 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) { Write-Host "[$($p.Name)] apksigner: signature INVALID" -ForegroundColor Red; $ok = $false }
    }
    if (-not $ok) { $failed = $true; continue }

    $out  = Join-Path $dist ("$appName-$buildVersion-$($p.Name).apk")
    Copy-Item $apk.FullName $out -Force
    $hash = (Get-FileHash $out -Algorithm SHA256).Hash.ToLower()
    $mb   = [math]::Round((Get-Item $out).Length / 1MB, 1)
    $state = if ($isUnsigned) { "UNSIGNED (cannot be installed)" } else { "signed" }
    $color = if ($isUnsigned) { "Yellow" } else { "Green" }
    Write-Host "[$($p.Name)] -> $out  ($mb MB, $state)" -ForegroundColor $color
    Write-Host "           SHA-256 $hash"
    $sums.Add("$hash  $(Split-Path $out -Leaf)")
}

if ($failed) { throw "One or more APKs failed verification. Do not distribute them." }
if ($sums.Count -eq 0) { throw "No APKs were produced." }
[IO.File]::WriteAllLines((Join-Path $dist "SHA256SUMS.txt"), $sums, (New-Object Text.UTF8Encoding $false))
Write-Host "`nAll APKs verified. Files are in $dist (hashes in SHA256SUMS.txt)" -ForegroundColor Green