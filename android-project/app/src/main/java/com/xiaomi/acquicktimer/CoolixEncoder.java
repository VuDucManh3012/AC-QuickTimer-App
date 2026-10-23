package com.xiaomi.acquicktimer;

/**
 * Bộ mã hóa đầy đủ cho giao thức COOLIX 24-bit (Funiki, Casper, các máy gốc Midea).
 * Theo đặc tả IRremoteESP8266 (ir_Coolix.h / ir_Coolix.cpp).
 *
 * Khung trạng thái: [0xB2][Fan 3 bit | SensorTemp 5 bit][Temp 4 bit | Mode 2 bit | 00]
 * Bật máy = phát khung trạng thái; Tắt/Swing/Sleep/Turbo... là các lệnh cố định.
 */
public final class CoolixEncoder {

    public static final int TEMP_MIN = 17;
    public static final int TEMP_MAX = 30;

    // Lệnh cố định
    public static final int CMD_OFF = 0xB27BE0;
    public static final int CMD_SWING = 0xB26BE0;
    public static final int CMD_SWING_STEP = 0xB20FE0;
    public static final int CMD_SLEEP = 0xB2E003;
    public static final int CMD_TURBO = 0xB5F5A2;
    public static final int CMD_LED = 0xB5F5A5;
    public static final int CMD_CLEAN = 0xB5F5AA;

    // Mã nhiệt độ 17 → 30°C
    private static final int[] TEMP_MAP = {
            0b0000, 0b0001, 0b0011, 0b0010, 0b0110, 0b0111, 0b0101,
            0b0100, 0b1100, 0b1101, 0b1001, 0b1000, 0b1010, 0b1011
    };
    private static final int FAN_MODE_TEMP_CODE = 0b1110; // Chế độ Quạt không có nhiệt độ
    private static final int SENSOR_TEMP_IGNORE = 0b11111;
    // Quạt theo thứ tự [Auto, Thấp, Vừa, Cao]; Auto tính riêng theo chế độ
    private static final int[] FAN_BITS = {0, 0b100, 0b010, 0b001};

    private CoolixEncoder() {
    }

    /**
     * Mã hóa khung trạng thái 24-bit.
     *
     * @param mode     "COOL", "HEAT", "DRY", "FAN" hoặc "AUTO"
     * @param temp     nhiệt độ °C, tự giới hạn trong 17–30
     * @param fanIndex 0 Auto, 1 Thấp, 2 Vừa, 3 Cao
     */
    public static int encodeState(String mode, int temp, int fanIndex) {
        int t = Math.min(TEMP_MAX, Math.max(TEMP_MIN, temp));
        boolean isFanMode = "FAN".equals(mode);
        int tempCode = isFanMode ? FAN_MODE_TEMP_CODE : TEMP_MAP[t - TEMP_MIN];

        // Auto/Dry dùng quạt "Auto0" (0b000), các chế độ khác dùng "Auto" (0b101)
        boolean autoOrDry = "AUTO".equals(mode) || "DRY".equals(mode);
        int fan = (fanIndex <= 0 || fanIndex >= FAN_BITS.length)
                ? (autoOrDry ? 0b000 : 0b101)
                : FAN_BITS[fanIndex];

        return (0xB2 << 16) | (((fan << 5) | SENSOR_TEMP_IGNORE) << 8) | (tempCode << 4) | (modeBits(mode) << 2);
    }

    /**
     * Khung hẹn giờ TẮT (COOLIX48, 6 byte), suy từ mã bắt được trên remote Midea thật
     * (IRremoteESP8266 issue #1824, vd. 0,5h COOL 21°C = B2 4D A1 5E 60 7F):
     * B2 4D | T ~T | [Temp 4 bit | Mode 2 bit | Khoảng 2 bit] | 7F
     * T = 0xA1 + 2 * (nấc 30 phút trong khoảng - 1); Khoảng = 0 (≤8h), 1 (≤16h), 2 (≤24h).
     * Hủy hẹn giờ: remote thật chỉ phát lại khung trạng thái thường (encodeState).
     */
    public static byte[] offTimerFrame(String mode, int temp, int minutes) {
        int halfHours = Math.max(1, Math.min(48, Math.round(minutes / 30f)));
        int range = (halfHours - 1) / 16;
        int step = halfHours - range * 16; // 1..16
        int t = 0xA1 + 2 * (step - 1);
        int clamped = Math.min(TEMP_MAX, Math.max(TEMP_MIN, temp));
        int tempCode = "FAN".equals(mode) ? FAN_MODE_TEMP_CODE : TEMP_MAP[clamped - TEMP_MIN];
        int stateByte = (tempCode << 4) | (modeBits(mode) << 2) | range;
        return new byte[]{(byte) 0xB2, (byte) 0x4D, (byte) t, (byte) ~t, (byte) stateByte, (byte) 0x7F};
    }

    /** Chuỗi xung phát qua ConsumerIrManager (dùng timing Coolix đã kiểm chứng trên máy thật). */
    public static int[] pattern(int raw) {
        return AcProtocolDatabase.encodeCoolixMsb(
                (byte) ((raw >> 16) & 0xFF), (byte) ((raw >> 8) & 0xFF), (byte) (raw & 0xFF));
    }

    // Chế độ Quạt được mã hóa thành Dry + temp code riêng
    private static int modeBits(String mode) {
        switch (mode) {
            case "DRY":
            case "FAN":
                return 0b01;
            case "AUTO":
                return 0b10;
            case "HEAT":
                return 0b11;
            default:
                return 0b00; // COOL
        }
    }
}
