# PROJECT_SETUP.md

## Prerequisites

- Android Studio Ladybug (2024.2) or newer
- Android SDK 35
- Android NDK (side-by-side) 26+
- CMake 3.22.1+
- JDK 17
- A physical Android device (ARM64 recommended) or emulator (x86_64)

## Build Setup

### 1. Clone llama.cpp

The native LLM inference engine depends on llama.cpp source code.

```bash
cd app/src/main/cpp
git clone https://github.com/ggerganov/llama.cpp.git
```

### 2. Set SDK Path

Create or edit `local.properties` in the project root:

```
sdk.dir=C\:\\Users\\<YOUR_USERNAME>\\AppData\\Local\\Android\\Sdk
```

### 3. Build the Project

Open the project in Android Studio, or build from command line:

```bash
./gradlew assembleDebug
```

The first build will compile llama.cpp native libraries for all target ABIs (arm64-v8a, armeabi-v7a, x86_64). This may take several minutes.

## Model Setup

### Download a GGUF Model

Download a quantized GGUF model file. Recommended:

- **Qwen3-4B-Q4_K_M.gguf** (~2.5 GB) — Primary target
- **Qwen3-1.7B-Q4_K_M.gguf** (~1.1 GB) — Lighter alternative

Sources:
- https://huggingface.co/Qwen
- Search HuggingFace for "Qwen3 GGUF"

### Transfer Model to Device

Option A — Copy to Downloads:
```bash
adb push Qwen3-4B-Q4_K_M.gguf /sdcard/Download/
```

Option B — Use the in-app file picker to import from any location.

## App Permissions

### Enable Accessibility Service

1. Open the app
2. Tap "Enable Accessibility" or go to:
   Settings → Accessibility → Android Agent
3. Enable the service
4. Confirm the permission dialog

### Screen Capture Permission

1. In the app, tap "Capture" to enable screen capture
2. Accept the system permission dialog
3. A notification will appear while capture is active

### Storage Permission

On Android 13+, grant "Allow access to media" when prompted for model file import.

## Running

1. Launch the app
2. Go to Model Manager (chip icon in toolbar)
3. Select and load a GGUF model
4. Ensure Accessibility Service is enabled (green status)
5. Type a task in the input field
6. Tap "Start Agent"

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest
```

## Architecture

```
Clean Architecture + MVVM

domain/     — Models, repository interfaces, use cases
data/       — Implementations (accessibility, capture, OCR, LLM, agent, memory, safety)
presentation/ — Jetpack Compose UI, ViewModels, theme
di/         — Hilt dependency injection modules
```

## Troubleshooting

### Build fails with CMake errors
- Ensure NDK 26+ is installed via SDK Manager
- Ensure CMake 3.22.1 is installed via SDK Manager
- Verify llama.cpp is cloned into `app/src/main/cpp/llama.cpp`

### Model fails to load
- Verify the file is a valid GGUF file (starts with "GGUF" magic bytes)
- Check device has sufficient RAM (model size + ~500MB overhead)
- Try a smaller quantization (Q4_K_M or Q3_K_S)

### Accessibility Service keeps disconnecting
- Disable battery optimization for Android Agent
- Settings → Apps → Android Agent → Battery → Unrestricted
