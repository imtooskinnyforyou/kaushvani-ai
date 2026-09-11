# KAARIGAR CI/CD Automation Guide

This repository is configured with automated GitHub Actions workflows to build, test, lint, and package the **KAARIGAR** Android application on every push and pull request.

---

## 🚀 Workflow Highlights (`.github/workflows/android-ci.yml`)

### 1. **Automated Triggers**
- **Push & Pull Requests**: Triggers automatically on changes to `main`, `master`, and `develop` branches.
- **Manual Trigger (`workflow_dispatch`)**: Allows on-demand triggers directly from the GitHub Actions UI with custom parameters:
  - Choose `build_type`: `debug`, `release`, or `both`
  - Choose `run_roborazzi`: enable/disable screenshot comparison testing

---

### 2. **Pipeline Stages**

| Stage | Action / Task | Output / Artifact |
| :--- | :--- | :--- |
| **Lint & Code Quality** | `gradle :app:lintDebug` | HTML & XML lint reports (`android-lint-reports`) |
| **Unit & Robolectric Tests** | `gradle :app:testDebugUnitTest` | JUnit XML & HTML test reports (`unit-test-reports`) |
| **Debug APK Assembly** | `gradle :app:assembleDebug` | Installable Debug APK (`kaarigar-debug-apk`) |
| **Release APK & AAB Bundle** | `gradle :app:assembleRelease :app:bundleRelease` | Signed Release APK + Play Store AAB (`kaarigar-release-artifacts`) |

---

## 🔐 GitHub Secrets Configuration

To enable full release signing and Gemini AI features in CI/CD builds, configure the following secrets in **Repository Settings → Secrets and variables → Actions**:

| Secret Name | Description | Required For |
| :--- | :--- | :--- |
| `GEMINI_API_KEY` | Google Gemini API Key for AI features | Catalog generation & AI pricing |
| `KEYSTORE_BASE64` | Base64-encoded release `.jks` keystore | Signed release APK & AAB generation |
| `STORE_PASSWORD` | Password for the release keystore | Release signing |
| `KEY_PASSWORD` | Password for the signing key alias | Release signing |

> **Note:** If release secrets are not configured, the pipeline gracefully builds the debug APK and executes all lint and unit test checks without failing.

---

## 📦 Accessing Build Artifacts

1. Navigate to the **Actions** tab on your GitHub repository.
2. Select the latest workflow run.
3. Scroll to the **Artifacts** section at the bottom to download:
   - `kaarigar-debug-apk`
   - `kaarigar-release-artifacts`
   - `unit-test-reports`
   - `android-lint-reports`
