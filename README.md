# ❄️ AC QuickTimer & Mi Remote (Android IR Blaster)

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-ED8B00.svg?style=flat&logo=java)](https://java.com)
[![IR](https://img.shields.io/badge/Hardware-IR%20Blaster-FF6900.svg?style=flat)](https://developer.android.com/reference/android/hardware/ConsumerIrManager)
[![Author](https://img.shields.io/badge/Author-VuDucManh3012-007ACC.svg?style=flat&logo=github)](https://github.com/VuDucManh3012)

Ứng dụng Android điều khiển điều hòa qua mắt hồng ngoại (IR Blaster) trên điện thoại Xiaomi / Android, tích hợp tính năng **1-Chạm Routine Hẹn Giờ Tắt Đêm** và giao diện **Mi Remote Toàn Diện** chuẩn MIUI.

---

## 🚀 Tính năng chính

- ⚡ **1-Chạm Routine:** Chỉ 1 nút bấm trước khi ngủ để tự động đếm ngược và tắt điều hòa lúc 04:00 sáng.
- 🎮 **Mi Remote Toàn Diện:** Giao diện điều khiển dạng lưới phẳng chuẩn Xiaomi (Bật/Tắt, Chế độ, Quạt gió, Đảo gió Swing, Tăng/Giảm nhiệt độ).
- 🔍 **Bộ Dò Mã Sóng IR:** Quét và kiểm tra tự động các bộ mã sóng phổ biến (Funiki, Midea, Daikin, Panasonic, LG, Gree...).
- 📱 **Widget Tiện Ích:** Đặt phím tắt nhanh 1-Chạm ra màn hình chính điện thoại.

---

## 📲 Cài đặt nhanh

Tải trực tiếp file APK đã biên dịch sẵn trong thư mục gốc:
👉 **[Tải ACQuickTimer.apk](./ACQuickTimer.apk)**

---

## 📑 Báo cáo chi tiết dự án

Xem tài liệu báo cáo kỹ thuật, cấu trúc giao thức Coolix 24-bit và quá trình xử lý lỗi tại:  
👉 **[Xem REPORT.md](./REPORT.md)**

---

## 🛠️ Biên dịch từ mã nguồn

```bash
cd android-project
# Windows
gradlew.bat assembleDebug

# Linux / macOS
./gradlew assembleDebug
```
File APK sẽ được xuất tại `app/build/outputs/apk/debug/app-debug.apk`.
