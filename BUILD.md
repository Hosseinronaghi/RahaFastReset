# Build Raha Fast Reset

The GitHub Actions workflow creates a single debug APK for Android.

1. Open **Actions** in this repository.
2. Choose **Build Raha Fast Reset Unified APK**.
3. Choose **Run workflow**, or inspect the automatic build on pushes to main.
4. Download the artifact `Raha-Fast-Reset-v1.0.1-Debug` from a successful run.
5. The archive contains `Raha-Fast-Reset-v1.0.1-debug.apk`.

Local build: install JDK 17, Android SDK API 35, Gradle 8.10.2, and run `gradle :app:assembleDebug`.

**Security:** Debug APKs are test builds, not signed release distributions. Factory reset is irreversible. Test only on a disposable device. This project targets SDK 32 for compatibility and does not guarantee reset permission on newer Android versions.
