# Life in the UK

An offline Android study aid built with Kotlin, Jetpack Compose and Material 3. The app is independent and is not an official UK government app. The included questions are original illustrative examples, not official test questions, and success in this app does not guarantee success in the real test.

## Project structure

- `app/src/main/java/.../data`: question model, validated asset loader and DataStore progress repository.
- `app/src/main/java/.../domain`: answer and score calculations.
- `app/src/main/java/.../ui`: ViewModel and observable screen state.
- `app/src/main/assets/questions.json`: editable offline question bank.
- `app/src/test`: local logic tests; `app/src/androidTest`: Compose practice-flow test.

## Requirements and first setup

Android Studio is installed on the development PC and includes Java 25. The Android SDK is at `%LOCALAPPDATA%\Android\Sdk`; API 35 and 37, build tools 36.0.0, emulator and `adb` are present. `JAVA_HOME`, `ANDROID_HOME` and Android tools are not on the command-line PATH. The SDK command-line tools package is missing. No separate Gradle installation is needed because the Gradle wrapper is included. This project was built using the bundled JDK 25.

1. In Android Studio, open **More Actions > SDK Manager**. Install **Android SDK Command-line Tools (latest)** and confirm the Android SDK Platform and Build Tools are selected.
2. In **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**, select the Android Studio bundled JDK. The project targets Java 17 bytecode; Gradle 9.6 was verified using the bundled JDK 25.
3. For command-line builds in the current PowerShell window, set these variables first:

   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
   $env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
   ```

   Optionally add `%LOCALAPPDATA%\Android\Sdk\platform-tools` and `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\bin` to your user PATH. Then open a new PowerShell window and try `adb version` and `sdkmanager --version`.
4. Open this folder in Android Studio and allow Gradle sync to download any dependencies not already cached. A network connection may be needed for initial setup; the app's study features do not use the network.

The detected Android Studio executable is `C:\Program Files\Android\Android Studio\bin\studio64.exe`. No standalone Gradle installation was found or needed because `gradlew.bat` and its wrapper JAR are included.

## Build and run

After Gradle sync completes in Android Studio, use **Run** and select an Android emulator, or connect a phone with Developer options and USB debugging enabled. To build from PowerShell, set the environment variables above and run:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

The app requires Android 8.0 (API 26) or later and requests no runtime permissions. Its project name and Android application ID are both `lifeintheuk.myapp`.

## Question JSON format

Keep question IDs unique. `correctAnswer` is a zero-based option index and exactly four non-empty answer strings are required.

```json
{
  "id": "sample-001",
  "question": "Which city is the capital of Scotland?",
  "options": ["Glasgow", "Edinburgh", "Aberdeen", "Dundee"],
  "correctAnswer": 1,
  "explanation": "Edinburgh is the capital city of Scotland.",
  "topic": "Nations and places"
}
```

The app validates all records on load. It uses the full set of 24 original illustrative records for mock mode, with 45 minutes, navigation, early submission confirmation, and results/review. Replace or expand the question bank with content you have the right to use before any public release.

## Privacy and local storage

Progress and mock scores are stored in Android DataStore on the device. The app has no login, analytics, advertising, payment features or `INTERNET` permission. Reset local progress from Settings.

## Release preparation

1. The Android application ID is `lifeintheuk.myapp`. Keep it consistent when preparing a release; changing it later creates a different app listing.
2. In Android Studio choose **Build > Generate Signed Bundle / APK > Android App Bundle**. Create a new keystore in a secure location, use a strong unique password, and keep secure backups. Never commit the keystore or passwords to source control.
3. Keep the upload key private and configure Play App Signing when setting up the Play Console listing. Review the current Play requirements, privacy disclosures and app content declarations before submission. Store policy approval is not guaranteed.
4. The app disclaimer is shown in Settings and on the home screen. Do not describe the sample set as official and do not claim that practice results guarantee passing the real test.

## Build configuration

The project pins Android Gradle Plugin 9.4.1, Gradle 9.6.0, Kotlin 2.4.10 and Compose BOM 2025.10.00. SDK compile level is 37, target level is 35, and minimum level is 26. No signing credentials are included.
