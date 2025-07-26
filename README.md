# 📱 Activity Detector App

An Android app built using Kotlin and Jetpack Compose that uses the device's accelerometer along with a Kalman filter to detect human activities in real time.

### 🚶‍♂️ Recognizes:
- **Standing Still**
- **Walking**
- **Running**
- **Climbing Up Stairs**
- **Climbing Down Stairs**

---

## 🔧 Features

- **Kalman Filter Smoothing** – Reduces sensor noise for reliable activity classification.
- **Variance-Based Detection** – Uses statistical variance to distinguish activity states.
- **Jetpack Compose UI** – Modern, reactive user interface built with Compose.
- **Real-Time Feedback** – Displays both smoothed accelerometer values and current activity.

---

## 📷 Screenshots

> _Add screenshots of your app UI here (optional)_

---

## 🚀 How It Works

### 🧠 Kalman Filter
Applies a Kalman filter to smooth noisy accelerometer data for each of the X, Y, and Z axes.

### 📊 Activity Detection Logic
- Computes variance of recent accelerometer readings (window of 50 samples).
- Calculates mean Z-axis value.
- Classifies activity based on the following rules:

```text
If variance < 0.5         → Standing Still  
If 0.5 <= variance <= 2.0 → 
    Z > 10.5              → Climbing Up  
    Z < 9.0               → Climbing Down  
    Else                  → Walking  
If variance > 2.0         → Running
```

---

## 🏗 Tech Stack

- **Language**: Kotlin  
- **UI Framework**: Jetpack Compose  
- **Sensors**: Android Accelerometer  
- **Architecture**: MVVM (lightweight)  

---

## 📂 Project Structure

```text
├── MainActivity.kt           # Activity with sensor handling and UI
├── KalmanFilter.kt           # Kalman filter implementation
├── ui.theme/                 # App theme files
├── build.gradle              # Project dependencies
```

---

## 📲 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/activity-recognition-app.git
   ```
2. Open in **Android Studio**.
3. Connect a device or use an emulator (with accelerometer support).
4. Run the app.

---

## ✍️ Author

**Naveen Kumaran**  
_BTech ECE @ NIT Warangal_  
[GitHub](https://github.com/your-username)

---

## 🪄 Future Improvements

- Use gyroscope for better climbing detection.
- Integrate machine learning model (e.g., TFLite) for advanced classification.
- Export sensor data to CSV.
- Visual graphs of live accelerometer signals.

---

## 📄 License

MIT License – use it freely.
