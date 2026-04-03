# 🚀 BUILD & RUN GUIDE - Timetable App v1.2

## Overview
This guide walks you through building and running the Timetable App v1.2 on an Android emulator.

---

## Prerequisites

### Required Software
- ✅ Java 17+ (verify with: `java -version`)
- ✅ Android SDK 34 (API level 34)
- ✅ Android NDK (optional)
- ✅ Gradle 8.0+ (bundled with project)
- ✅ Kotlin 1.9.24+ (bundled with project)

### System Requirements
- **OS**: macOS (as in your case)
- **RAM**: 8GB minimum, 16GB recommended
- **Disk Space**: 10GB minimum
- **Time**: First build ~5-10 minutes, subsequent builds ~1-2 minutes

---

## Step 1: Setup Android Emulator

### Option A: Using Android Studio
```
1. Open Android Studio
2. Click "Tools" → "Device Manager"
3. Click "Create Device"
4. Select Pixel 6 Pro (or any device)
5. Select API level 33 or 34 (Android 13+)
6. Click "Create"
7. Click Play button to start emulator
8. Wait for emulator to fully load (1-2 minutes)
```

### Option B: Using Command Line
```bash
# List available emulators
emulator -list-avds

# Start specific emulator (replace EMULATOR_NAME)
emulator -avd EMULATOR_NAME &

# Wait for emulator to fully boot before proceeding
adb wait-for-device
```

### Verify Emulator is Ready
```bash
adb devices
# Should show device like: emulator-5554    device
```

---

## Step 2: Prepare Project

```bash
# Navigate to project
cd /Users/abhishekmeena/AndroidStudioProjects/Timetable

# Clean previous builds
./gradlew clean

# (Optional) Update dependencies
./gradlew --refresh-dependencies
```

---

## Step 3: Build APK

### Full Debug Build
```bash
./gradlew build
```

### Faster Build (Skip Tests)
```bash
./gradlew build -x test
```

### Monitor Build Progress
```bash
# Build with detailed output
./gradlew build --info 2>&1 | grep -E "Task|Completed|error"
```

### Expected Output
```
��� BUILD SUCCESSFUL
Total time: X.XXX secs
```

**Typical Build Time**: 3-5 minutes (first time), 1-2 minutes (subsequent)

---

## Step 4: Install on Emulator

### Automatic Install & Run
```bash
./gradlew installDebug
```

### OR Manual Install
```bash
# Build APK
./gradlew build

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.example.timetable/.MainActivity
```

---

## Step 5: Run the App

### Automatic (Gradle)
```bash
./gradlew installDebug
```

### Manual (ADB)
```bash
# Start activity
adb shell am start -n com.example.timetable/.MainActivity

# View logs
adb logcat | grep timetable
```

---

## Testing the App

### Quick Feature Test
```
1. App launches → Dashboard visible
2. Click Schedule icon → Schedule screen opens
3. Click Edit button → Weekly Schedule Editor opens
4. Try adding a class:
   - Subject: "Test Class"
   - Time: 09:30 - 10:30
   - Room: "L-15"
   - Click Save
5. Verify class appears in list
```

### Verify Features
- ✅ Dashboard loads
- ✅ Schedule shows classes
- ✅ Weekly editor works
- ✅ Can add/edit/delete classes
- ✅ Colors display correctly
- ✅ Navigation works smoothly

---

## Troubleshooting

### Build Fails

**Issue**: "Task 'compileDebugKotlin' not found"
```bash
Solution: ./gradlew clean build
```

**Issue**: "No Android SDK found"
```bash
Solution: 
1. Open Android Studio
2. Go to Preferences → SDK Manager
3. Install API 33 and 34
4. Note the SDK path
5. Create local.properties in project root:
   sdk.dir=/path/to/android/sdk
```

**Issue**: "Insufficient disk space"
```bash
Solution: Delete old builds
./gradlew clean
rm -rf ~/.gradle/caches
```

### Installation Fails

**Issue**: "Device not found"
```bash
Solution: 
1. Verify emulator is running: adb devices
2. Start emulator if needed
3. Wait for boot completion
4. Try again
```

**Issue**: "INSTALL_FAILED_VERSION_DOWNGRADE"
```bash
Solution: Uninstall previous version
adb uninstall com.example.timetable
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Issue**: "INSTALL_FAILED_INSUFFICIENT_STORAGE"
```bash
Solution: Clear emulator storage
adb shell pm clear com.example.timetable
# Or restart emulator
```

### App Crashes

**Issue**: "App crashes on startup"
```
1. Check logs: adb logcat | grep timetable
2. Look for errors in AndroidManifest.xml
3. Verify minSdk is 33+
4. Try: adb shell pm clear com.example.timetable
```

**Issue**: "ClassNotFoundException"
```
Solution: 
1. Clean build: ./gradlew clean build
2. Restart emulator
3. Reinstall: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Advanced Options

### Build Variants
```bash
# Release build (requires signing)
./gradlew assembleRelease

# Debug build with ProGuard
./gradlew assembleDebug -Pandroid.enableProguardInReleaseBuilds=true
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### View Build Info
```bash
# Check dependencies
./gradlew dependencies

# Check build cache
./gradlew buildEnvironment

# Analyze APK size
./gradlew analyzeApk
```

---

## Performance Tips

### Faster Builds
1. **Parallel Builds**: Add to `gradle.properties`
   ```
   org.gradle.parallel=true
   org.gradle.workers.max=8
   ```

2. **Instant Run** (if using Android Studio)
   - Just run app from IDE with "Run" button
   - Changes appear instantly

3. **Incremental Compilation**
   - Only modified files are recompiled
   - Clean build only when needed

### Emulator Performance
1. **Allocate Resources**
   - RAM: 4GB minimum, 8GB recommended
   - Storage: 10GB minimum
   - Cores: 4+ CPU cores

2. **Enable Hardware Acceleration**
   - HAXM on Intel Mac
   - Hypervisor Framework on Apple Silicon

3. **Use Pixel 4a** (fastest emulator)
   - Lighter than Pixel 6 Pro
   - Still has API 33+

---

## Build Commands Cheat Sheet

```bash
# Full clean build and install
./gradlew clean build installDebug

# Just build
./gradlew build

# Just install
./gradlew installDebug

# Build and install (fastest)
./gradlew installDebug -x test

# Run with logging
./gradlew build -i

# Build specific variant
./gradlew assembleDebug

# Clean project
./gradlew clean

# Show gradle tasks
./gradlew tasks

# Update dependencies
./gradlew --refresh-dependencies
```

---

## Post-Installation Checklist

- ✅ App installs without errors
- ✅ App launches successfully
- ✅ All screens are accessible
- ✅ Weekly Schedule Editor works
- ✅ Forms validate properly
- ✅ Colors display in light mode
- ✅ Colors display in dark mode
- ✅ Navigation is smooth
- ✅ No crashes or errors

---

## Logs & Debugging

### View Logs
```bash
# All logs
adb logcat

# App-specific logs
adb logcat | grep timetable

# Save logs to file
adb logcat > /tmp/app_logs.txt

# Clear logs
adb logcat -c
```

### Logcat Filters
```bash
# Errors only
adb logcat *:E | grep timetable

# Warnings and errors
adb logcat *:W | grep timetable

# Specific tag
adb logcat -s TAG_NAME
```

---

## Testing Scenarios

### Scenario 1: Basic Navigation
```
1. Launch app
2. Tap Home icon
3. Tap Schedule icon
4. Tap Mess icon
5. Tap Profile icon
6. Verify all screens load
```

### Scenario 2: Weekly Schedule Editor
```
1. Go to Schedule
2. Click Edit button
3. Add a class
4. Edit the class
5. Delete the class
6. Copy to next day
7. Save changes
```

### Scenario 3: Form Validation
```
1. Try to add class without subject (should fail)
2. Try invalid time format like "25:00" (should fail)
3. Try valid format "09:30" (should pass)
4. Verify error messages display
```

### Scenario 4: Color Modes
```
1. Settings → Developer Options → Set Light Theme
2. Verify light colors display
3. Settings → Set Dark Theme
4. Verify dark colors display
5. Verify readability in both modes
```

---

## Success Indicators

### Build Complete
```
✅ BUILD SUCCESSFUL
Total time: X.XXXs
```

### Installation Complete
```
✅ Success
```

### App Running
```
✅ App appears on emulator
✅ Dashboard visible
✅ Can navigate between screens
✅ Weekly editor accessible
```

---

## Next Steps

After successful installation:
1. ✅ Explore all features
2. ✅ Test form validation
3. ✅ Check colors and theming
4. ✅ Verify navigation
5. ✅ Look for crashes in logs
6. ✅ Report any issues

---

## Support

If you encounter issues:
1. Check the **Troubleshooting** section
2. Review the logs: `adb logcat | grep timetable`
3. Try clean build: `./gradlew clean build`
4. Restart emulator and reinstall
5. Check Android version: API 33+ required

---

## Version Information
- **App Version**: 1.2
- **Min SDK**: 33 (Android 13)
- **Target SDK**: 34
- **Java**: 17+
- **Kotlin**: 1.9.24

---

**Ready to build and run!** 🚀

Follow the steps above and you'll have the Timetable App v1.2 running on your emulator in minutes.


