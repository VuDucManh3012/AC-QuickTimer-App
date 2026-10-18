# BÁO CÁO DỰ ÁN: AC QUICKTIMER & MI REMOTE (ANDROID IR BLASTER)

> **Dự án:** Ứng dụng điều khiển điều hòa Funiki & Đa năng qua mắt hồng ngoại (IR Blaster) với tính năng 1-Chạm Routine Hẹn Giờ Tắt Đêm và Remote Toàn Diện phong cách Xiaomi MIUI.  
> **Tác giả:** Vũ Đức Mạnh (GitHub: [@VuDucManh3012](https://github.com/VuDucManh3012))  
> **Nền tảng:** Android Java Native (minSdk 24, targetSdk 34)  
> **Ngày cập nhật:** 19/10/2026  

---

## 1. TỔNG QUAN & ĐẶT VẤN ĐỀ

### 1.1. Bối cảnh
Nhiều dòng điều hòa phổ biến tại Việt Nam (đặc biệt là Funiki / Midea) sử dụng remote không có tính năng hẹn giờ theo mốc thời gian thực hoặc quy trình cài đặt hẹn giờ phức tạp. Khi ngủ ban đêm, việc điều hòa bật suốt đêm thường gây lạnh buốt lúc 3 - 5 giờ sáng, lãng phí điện năng và ảnh hưởng xấu đến sức khỏe đường hô hấp.

### 1.2. Mục tiêu dự án
1. **1-Chạm Routine (Quick Timer):** Chỉ cần bấm 1 nút trước khi ngủ, ứng dụng tự động tính khoảng thời gian đến mốc giờ mong muốn (mặc định 04:00 sáng) và tự động kích hoạt hồng ngoại tắt điều hòa chính xác đến từng giây.
2. **Bộ Dò Mã Sóng Hồng Ngoại (Smart IR Scanner):** Quét qua các bộ mã phổ biến trên thị trường để tìm đúng tần số và giao thức điều hòa mà không cần remote gốc để học lệnh.
3. **Mi Remote Toàn Diện (Full Controller):** Cung cấp giao diện điều khiển bàn phím lưới phẳng chuẩn Xiaomi (Bật/Tắt, Chế độ, Tốc độ gió, Đảo gió Swing, Chỉnh nhiệt độ `+`/`-`, Hẹn giờ, Chế độ ngủ Sleep).
4. **Widget Màn Hình Khóa / Home Screen:** Cho phép thao tác bật hẹn giờ ngay từ màn hình chính mà không cần mở ứng dụng.

---

## 2. KIẾN TRÚC HỆ THỐNG & CÔNG NGHỆ

```
                ┌───────────────────────────────────────────────┐
                │          GIAO DIỆN NGƯỜI DÙNG (UI/UX)         │
                ├──────────────┬──────────────┬─────────────────┤
                │  ⚡ 1-Chạm    │  🎮 Remote Mi │   🔍 Dò Mã IR   │
                │    Routine   │   Grid View  │    Brand DB     │
                └──────┬───────┴──────┬───────┴────────┬────────┘
                       │              │                │
                       ▼              ▼                ▼
                ┌───────────────────────────────────────────────┐
                │         CORE LOGIC: MainActivity.java         │
                │  - State & Preferences Manager                │
                │  - Precise Alarm Scheduler (AlarmManager)     │
                │  - Dynamic Theme & UI Renderer                │
                └─────────────────────┬─────────────────────────┘
                                      │
                       ┌──────────────┴──────────────┐
                       ▼                             ▼
        ┌────────────────────────────┐  ┌────────────────────────────┐
        │  AcProtocolDatabase.java   │  │    IrTransmitter.java      │
        │  - Database Coolix 24-bit  │  │  - Android ConsumerIrMgr   │
        │  - Daikin, Panasonic, LG...│  │  - 38 kHz Carrier Modulator│
        └────────────────────────────┘  └─────────────┬──────────────┘
                                                      ▼
                                       ┌─────────────────────────────┐
                                       │   Mắt Hồng Ngoại Phần Cứng  │
                                       │   (Built-in IR Blaster)     │
                                       └─────────────────────────────┘
```

### 2.1. Ngăn xếp công nghệ (Tech Stack)
- **Ngôn ngữ:** Java Native (Android SDK 34, AndroidX, Material Components).
- **Hệ thống điều khiển phần cứng:** `android.hardware.ConsumerIrManager`.
- **Hệ thống hẹn giờ nền chính xác:** `AlarmManager` kết hợp `BroadcastReceiver` và `WakeLock`.
- **Công cụ build:** Gradle 8.6, Android Gradle Plugin 8.3.0.

---

## 3. GIẢI MÃ SÓNG HỒNG NGOẠI & GIAO THỨC COOLIX (FUNIKI)

### 3.1. Đặc tính giao thức Coolix
Dòng điều hòa Funiki sử dụng chung tập lệnh và vi điều khiển với Midea Coolix:
- **Tần số sóng mang (Carrier Frequency):** 38,000 Hz (38 kHz), Duty Cycle ~ 33%.
- **Khung truyền dẫn (Frame Format):**
  - **Header Pulse:** Mark 4,500 µs + Space 4,500 µs.
  - **Bit Logic '1':** Mark 560 µs + Space 1,690 µs (~2,250 µs).
  - **Bit Logic '0':** Mark 560 µs + Space 560 µs (~1,120 µs).
  - **Stop Bit:** Mark 560 µs.
  - **Gap giữa 2 khung:** Space ~7,000 µs.
  - **Quy tắc kiểm tra toàn vẹn:** Mỗi lệnh phát 2 frame 24-bit giống hệt nhau, trong đó mỗi byte dữ liệu đi kèm một byte đảo bit (`Bit-Inversion Check`) để chống nhiễu quang học.

### 3.2. Kết quả dò mã thực nghiệm thành công
Sau quá trình dò quét tự động trên thiết bị thực tế, hai mã lệnh chính xác tuyệt đối đã được xác định:
- **Mã Tắt Máy (Coolix OFF):** `0xB27BE0` (Được cấu hình làm Mã 5 trong cơ sở dữ liệu).
- **Mã Bật Máy (Coolix ON - 26°C Cool):** `0xB21F48` (Được cấu hình làm Mã 6 trong cơ sở dữ liệu).

---

## 4. QUÁ TRÌNH PHÁT TRIỂN & XỬ LÝ LỖI KỸ THUẬT (TROUBLESHOOTING)

### 4.1. Lỗi phân tách lệnh Bật và Tắt trong bộ hẹn giờ
- **Hiện tượng:** Tại màn hình dò mã, bấm Mã 5 thì điều hòa kêu bíp và tắt, Mã 6 thì kêu bíp và bật; nhưng khi sang tab Routine bấm hẹn giờ tắt thì điều hòa lại bật lên hoặc không phản hồi.
- **Nguyên nhân:** Biến `selectedPairingCode` ban đầu chỉ lưu 1 mã duy nhất đại diện cho hãng, dẫn tới logic hẹn giờ tắt nhầm sang mã bật nếu người dùng vừa test mã 6.
- **Giải pháp:** Tách rõ ràng cặp mã `codeOn` và `codeOff` trong `AcProtocolDatabase`. Tab Routine luôn ưu tiên gọi `sendPowerOff()` phát chuẩn xác chuỗi xung `0xB27BE0` khi kích hoạt tắt máy hoặc khi bộ đếm giờ kết thúc.

### 4.2. Lỗi giao diện: Android MaterialButton tự động đè màu cam (`backgroundTint`)
- **Hiện tượng:** Trên ứng dụng Android thực tế, các nút bấm bị phủ một lớp màu cam chói đặc kín, đè mất nền drawable thiết kế riêng; chữ và icon bị chìm; hai nút `-` và `+` nhiệt độ bị biến thành hai cục cam tròn xoe.
- **Nguyên nhân:** Theme ứng dụng kế thừa `Theme.MaterialComponents.DayNight.NoActionBar`. Thành phần Material Components tự động inject `backgroundTint = colorPrimary` (màu cam Xiaomi) vào tất cả các thẻ `<Button>`, phớt lờ thuộc tính `android:background`.
- **Giải pháp:**
  1. Thêm `<item name="backgroundTint">@null</item>` và `<item name="android:backgroundTint">@null</item>` vào `themes.xml`.
  2. Gán trực tiếp `android:backgroundTint="@null"` trên toàn bộ các thẻ Button trong `activity_main.xml`.
  3. Tạo bộ drawable tùy biến riêng: `btn_mini_temp.xml` (nền xám đậm bo góc), `btn_red_card.xml` (viền đỏ nổi bật cho Tắt Máy), `btn_card_button.xml` (thẻ công cụ), `mi_grid_cell.xml` (ô lưới Mi Remote có hiệu ứng bấm).

---

## 5. HỆ THỐNG GIAO DIỆN (UI/UX)

1. **Tab 1: ⚡ 1-Chạm Routine**
   - Đồng hồ số lớn hiển thị mốc giờ mục tiêu (mặc định 04:00 sáng).
   - Bộ đếm thời gian thực tự động tính: *Ví dụ: "Còn 3 giờ 15 phút sẽ tự tắt điều hòa"*.
   - Cụm điều khiển nhiệt độ mini: Nút `—` và `+` hiển thị nhiệt độ rõ ràng.
   - Nút Tắt Máy ngay lập tức (nền tối viền đỏ `#EF4444`).
   - Nút Hủy hẹn giờ khi không có nhu cầu.

2. **Tab 2: 🎮 Remote Mi**
   - Thiết kế mô phỏng 100% ứng dụng Mi Remote chính hãng Xiaomi:
     - Nửa trên: Màn hình điều hòa lớn hiển thị nhiệt độ `26°C`, icon bông tuyết `COOL`, tốc độ gió `AUTO`, trạng thái đảo gió `SWING`.
     - Nút Power tròn: Đổi màu xanh dạ quang khi Bật, màu xám khi Tắt.
     - Nửa dưới: Bàn phím lưới 9 ô phẳng: `⏻ Nguồn`, `Chế độ`, `Tốc độ`, `Hướng gió`, `Đảo gió`, `— Nhiệt độ +`, `Hẹn giờ`, `Ngủ`, `••• Khác`.

3. **Tab 3: 🔍 Dò Mã Sóng IR**
   - Bộ chọn thương hiệu (Funiki, Daikin, Panasonic, LG, Gree, Casper...).
   - Chế độ tự động quét mã (Auto Scan 1.5 giây/mã) kèm nút thử nghiệm Bật/Tắt tức thì.

4. **Tab 4: 📱 Hướng dẫn Widget**
   - Hướng dẫn đưa tiện ích 1-Chạm ra màn hình khóa / màn hình chính Android.

---

## 6. HƯỚNG DẪN CÀI ĐẶT & SỬ DỤNG

### 6.1. Cài đặt trực tiếp qua file APK
File APK đã được biên dịch hoàn chỉnh sẵn sàng sử dụng:
- **Tập tin:** `ACQuickTimer.apk` (Nằm ngay tại thư mục gốc của repository)
- **Cách cài đặt:**
  1. Tải file `ACQuickTimer.apk` vào điện thoại Android có mắt hồng ngoại (ví dụ: Xiaomi Redmi Note, POCO, Mi series).
  2. Mở file APK và chọn **Cài đặt** (Cho phép cài đặt từ nguồn không xác định nếu được hỏi).
  3. Mở ứng dụng và bắt đầu sử dụng ngay mà không cần cấp quyền rườm rà.

### 6.2. Biên dịch từ mã nguồn
```bash
# Clone repository
git clone https://github.com/VuDucManh3012/AC-QuickTimer-App.git

# Di chuyển vào thư mục Android
cd AC-QuickTimer-App/android-project

# Biên dịch bản Debug APK
./gradlew assembleDebug
# File xuất ra tại: app/build/outputs/apk/debug/app-debug.apk
```

---

## 7. KẾT LUẬN & ĐỊNH HƯỚNG MỞ RỘNG
Dự án đã giải quyết trọn vẹn nhu cầu hẹn giờ tắt điều hòa đêm thông minh bằng phần cứng IR Blaster có sẵn trên điện thoại, đồng thời mang lại trải nghiệm điều khiển toàn diện chuẩn Xiaomi MIUI. Các hướng phát triển tiếp theo:
- Tích hợp thêm cảm biến nhiệt độ môi trường nếu phần cứng hỗ trợ.
- Mở rộng thêm cơ sở dữ liệu các dòng điều hòa nội địa Nhật (Toshiba, Fujitsu, Mitsubishi).
