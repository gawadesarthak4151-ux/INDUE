# INDUE Android Assistant — native Kotlin starter

## Included
- Marathi-first speech recognition and spoken responses
- Tap-to-talk voice control
- Foreground-app wake phrase prototype: “Hey INDUE” / “INDUE”
- Camera launch intent
- Phone dialer intent (the user confirms the call)
- YouTube / browser launch
- Android Settings launch
- Basic date/time and help commands
- Activity configured to show over lock screen where Android/OEM policy permits

## Build an APK
1. Install Android Studio and Android SDK Platform 35.
2. Open this `INDUE_Android` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Connect an Android phone with USB debugging enabled, or use an emulator.
5. Run the `app` configuration.
6. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
7. Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Important Android limits
- Wake-word mode in this starter is foreground-only. It is not an always-on background hotword engine. Production background hotword requires a user-started microphone foreground service, persistent notification, compatible on-device keyword spotting, battery policy handling, and OEM testing.
- `showWhenLocked` does not bypass the keyguard. Android may require unlock before opening dialer, camera, or protected apps.
- Calls open the dialer; the app does not place calls silently.
- Speech recognition availability and Marathi support depend on the installed speech service and language packs.
- A conversational AI backend, contacts lookup, SMS, flashlight, alarms, robust multilingual parsing, and background wake-word engine are not included yet.
- This app does not secretly record audio or bypass Android security.
