# USER_ACTIONS.md

Things you need to do manually that the code cannot do for you.

## Required Before Building

### 1. Clone llama.cpp source code

```bash
cd app/src/main/cpp
git clone https://github.com/ggerganov/llama.cpp.git
```

This provides the C++ source for the local LLM inference engine. The CMake build system will compile it into native `.so` libraries during the Android build.

### 2. Set your Android SDK path

Create a file called `local.properties` in the project root with:

```
sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```

Replace `YOUR_USERNAME` with your actual Windows username.

### 3. Install required SDK components

In Android Studio SDK Manager, ensure you have:
- Android SDK Platform 35
- Android NDK (Side by side) — version 26 or newer
- CMake 3.22.1

## Required Before Running

### 4. Download a GGUF model file

Download one of these from HuggingFace:
- **Qwen3-4B-Q4_K_M.gguf** (~2.5 GB) — Best quality for mobile
- **Qwen3-1.7B-Q4_K_M.gguf** (~1.1 GB) — Faster, less accurate

Transfer to your device:
```bash
adb push Qwen3-4B-Q4_K_M.gguf /sdcard/Download/
```

Or use the in-app Model Manager to import from device storage.

### 5. Enable the Accessibility Service

After installing the app:
1. Open app → tap "Enable Accessibility"
2. Find "Android Agent" in the Accessibility settings list
3. Toggle it ON
4. Confirm the permission dialog

This is required for the agent to read screen content and perform actions.

### 6. Grant Screen Capture Permission

Tap "Capture" in the app and accept the system dialog. This enables OCR-based text recognition from screenshots.

### 7. Disable Battery Optimization (Recommended)

To prevent the system from killing the accessibility service:
- Settings → Apps → Android Agent → Battery → set to "Unrestricted"

## Optional

### 8. Import additional GGUF models

You can add any GGUF-format model. Place `.gguf` files in:
- Device Downloads folder (`/sdcard/Download/`)
- Or import via the in-app file picker

The app will automatically discover models in these locations.
