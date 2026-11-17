# Raha Fast Reset — v1.0.1 (Unified)

Independent Kotlin Android utility based on public Android DevicePolicyManager APIs. One package ID (`ir.raha.fastreset`) and one debug APK artifact.

## Features
- Persian and English interface, Material 3 light/dark themes
- Explicit Device Administrator enrollment, no network permission
- Device Owner `wipeDevice(0)` on Android 14+ where provisioned; legacy Device Admin `wipeData(0)` otherwise where permitted
- Foreground user-initiated reset, optional one-tap mode with prior irreversible-action consent
- Error handling, disable administration, data-loss notices

## Limitations
- Installing the APK **does not** make it Device Owner; management roles are provisioned externally.
- Target SDK 32 is a compatibility choice, not a guarantee it will work on modern Android, nor proof of Play Store eligibility.
- No bypass of FRP, user verification, manufacturer policy or Android security restrictions.
- Never test factory reset on a phone containing important data.
- **Build is not equivalent to successful device testing.**

See [BUILD.md](BUILD.md) for the build process.
