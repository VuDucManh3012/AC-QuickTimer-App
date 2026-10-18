package com.xiaomi.acquicktimer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AcProtocolDatabase {

    public static class CodeProfile {
        public int id;
        public String name;
        public String proto;

        public CodeProfile(int id, String name, String proto) {
            this.id = id;
            this.name = name;
            this.proto = proto;
        }
    }

    public static class BrandData {
        public String key;
        public String name;
        public int carrier;
        public List<CodeProfile> codes;

        public BrandData(String key, String name, int carrier) {
            this.key = key;
            this.name = name;
            this.carrier = carrier;
            this.codes = new ArrayList<>();
        }
    }

    public static final Map<String, BrandData> BRANDS = new LinkedHashMap<>();

    static {
        // =========================================================================
        // 1. FUNIKI (HÒA PHÁT) - 10 BỘ MÃ (CHUẨN COOLIX & MI REMOTE)
        // =========================================================================
        BrandData funiki = new BrandData("funiki", "Funiki (Hòa Phát)", 38000);

        // Mã 1-3: Chuẩn tuyệt đối dòng remote RG10B(E)/BGEF
        funiki.codes.add(new CodeProfile(1, "Funiki Mã 1 (Chuẩn Remote RG10B - Bật/Tắt Power)", "MIDEA_RG10_POWER"));
        funiki.codes.add(new CodeProfile(2, "Funiki Mã 2 (Chuẩn Remote RG10B - Làm Lạnh Cool 26°C)", "MIDEA_RG10_COOL26"));
        funiki.codes.add(new CodeProfile(3, "Funiki Mã 3 (Chuẩn Remote RG10B - Làm Lạnh Cool 25°C)", "MIDEA_RG10_COOL25"));

        // Mã 4: Mã số 4 trong Mi Remote Xiaomi
        funiki.codes.add(new CodeProfile(4, "Funiki Mã 4 (Mi Remote Bộ Mã 4 - Coolix 0xB2 0x4D 0xBF)", "COOLIX_4D_MSB"));

        // ⭐ Mã 5 & 6: ĐÃ KIỂM CHỨNG KÊU BÍP VÀ ĐIỀU KHIỂN ĐƯỢC ĐIỀU HÒA
        funiki.codes.add(new CodeProfile(5, "Funiki Mã 5 (Lệnh Tắt Máy - Coolix 0xB2 0x7B 0xE0)", "COOLIX_7B_MSB"));
        funiki.codes.add(new CodeProfile(6, "Funiki Mã 6 (Lệnh Bật Máy - Coolix 0xB2 0x1F 0x48)", "COOLIX_1F_MSB"));
        funiki.codes.add(new CodeProfile(7, "Funiki Mã 7 (Mi Remote Bộ Mã 3 - Coolix 0xB2 0xDF 0x20)", "COOLIX_DF_MSB"));

        // Mã 8-10: Các hệ bo mạch khác (Midea Classic, AUX, Gree)
        funiki.codes.add(new CodeProfile(8, "Funiki Mã 8 (Midea 48-bit Classic 0xA1 0x82 0x6F)", "MIDEA_CLASSIC_48"));
        funiki.codes.add(new CodeProfile(9, "Funiki Mã 9 (Funiki Bo Mạch AUX 0xC3)", "AUX_C3"));
        funiki.codes.add(new CodeProfile(10, "Funiki Mã 10 (Funiki Bo Mạch Gree 64-bit)", "GREE_64"));

        BRANDS.put("funiki", funiki);

        // 2. CASPER
        BrandData casper = new BrandData("casper", "Casper", 38000);
        casper.codes.add(new CodeProfile(1, "Casper Coolix", "COOLIX_1F_MSB"));
        casper.codes.add(new CodeProfile(2, "Casper Midea/RG10", "MIDEA_RG10_POWER"));
        BRANDS.put("casper", casper);

        // 3. PANASONIC
        BrandData panasonic = new BrandData("panasonic", "Panasonic", 38000);
        panasonic.codes.add(new CodeProfile(1, "Panasonic Inverter", "PANASONIC_27B"));
        BRANDS.put("panasonic", panasonic);

        // 4. DAIKIN
        BrandData daikin = new BrandData("daikin", "Daikin", 38000);
        daikin.codes.add(new CodeProfile(1, "Daikin ARC433", "DAIKIN_ARC433"));
        BRANDS.put("daikin", daikin);
    }

    /**
     * PHÁT MÃ THỬ CHO TỪNG BỘ MÃ TRONG TAB DÒ MÃ SÓNG
     */
    public static int[] buildPowerTogglePattern(BrandData brand, CodeProfile profile) {
        String proto = profile.proto;
        switch (proto) {
            case "COOLIX_1F_MSB":
                // ⭐ Mã 6: Lệnh BẬT MÁY Coolix đã kiểm chứng kêu BÍP và BẬT thành công!
                return encodeCoolixMsb((byte) 0xB2, (byte) 0x1F, (byte) 0x48);

            case "COOLIX_7B_MSB":
                // ⭐ Mã 5: Lệnh TẮT MÁY Coolix đã kiểm chứng kêu BÍP và TẮT thành công!
                return encodeCoolixMsb((byte) 0xB2, (byte) 0x7B, (byte) 0xE0);

            case "COOLIX_4D_MSB":
                return encodeCoolixMsb((byte) 0xB2, (byte) 0x4D, (byte) 0xBF);

            case "COOLIX_DF_MSB":
                return encodeCoolixMsb((byte) 0xB2, (byte) 0xDF, (byte) 0x20);

            case "MIDEA_RG10_POWER":
                return encodeMidea48((byte) 0xA1, (byte) 0x9C, (byte) 0x5E, (byte) 0xFF, (byte) 0xFF, (byte) 0x53);

            case "MIDEA_RG10_COOL26":
                return encodeMidea48((byte) 0xA1, (byte) 0xA0, (byte) 0x49, (byte) 0xFF, (byte) 0xFF, (byte) 0x67);

            case "MIDEA_RG10_COOL25":
                return encodeMidea48((byte) 0xA1, (byte) 0xA0, (byte) 0x48, (byte) 0xFF, (byte) 0xFF, (byte) 0x66);

            case "MIDEA_CLASSIC_48":
                return encodeMidea48((byte) 0xA1, (byte) 0x82, (byte) 0x6F, (byte) 0xFF, (byte) 0xFF, (byte) 0x62);

            case "AUX_C3":
                return encodeAuxMsb();

            case "GREE_64":
                return encodeGreeMsb();

            case "PANASONIC_27B":
                return buildPanasonicPattern();

            case "DAIKIN_ARC433":
                return buildDaikinPattern();

            default:
                return encodeCoolixMsb((byte) 0xB2, (byte) 0x1F, (byte) 0x48);
        }
    }

    /**
     * PHÁT MÃ TẮT NGUỒN (POWER OFF)
     */
    public static int[] buildPowerOffPattern(BrandData brand, CodeProfile profile) {
        if ("funiki".equals(brand.key) || profile.proto.startsWith("COOLIX")) {
            // ⭐ Lệnh Tắt điều hòa Coolix chuẩn (Mã 5: 0xB2 0x7B 0xE0) - ĐÃ KIỂM CHỨNG TẮT THÀNH CÔNG!
            return encodeCoolixMsb((byte) 0xB2, (byte) 0x7B, (byte) 0xE0);
        }
        if (profile.proto.startsWith("MIDEA")) {
            return encodeMidea48((byte) 0xA1, (byte) 0x1C, (byte) 0x5E, (byte) 0xFF, (byte) 0xFF, (byte) 0xD3);
        }
        return buildPowerTogglePattern(brand, profile);
    }

    /**
     * PHÁT MÃ BẬT NGUỒN (POWER ON)
     */
    public static int[] buildPowerOnPattern(BrandData brand, CodeProfile profile, int temp, int fanSpeed) {
        if ("funiki".equals(brand.key) || profile.proto.startsWith("COOLIX")) {
            // ⭐ Lệnh Bật điều hòa Coolix chuẩn (Mã 6: 0xB2 0x1F 0x48) - ĐÃ KIỂM CHỨNG BẬT THÀNH CÔNG!
            return encodeCoolixMsb((byte) 0xB2, (byte) 0x1F, (byte) 0x48);
        }
        if (profile.proto.startsWith("MIDEA")) {
            byte b5 = (byte) 0xA1;
            byte b4;
            switch (fanSpeed) {
                case 1: b4 = (byte) 0x88; break; // Fan Thấp
                case 2: b4 = (byte) 0x90; break; // Fan Vừa
                case 3: b4 = (byte) 0x98; break; // Fan Cao
                default: b4 = (byte) 0xA0; break; // Fan Auto
            }
            byte b3 = (byte) (0x40 | ((temp - 17) & 0x0F));
            byte b2 = (byte) 0xFF;
            byte b1 = (byte) 0xFF;
            byte b0 = calcMideaChecksum(b5, b4, b3, b2, b1);
            return encodeMidea48(b5, b4, b3, b2, b1, b0);
        }
        return buildPowerTogglePattern(brand, profile);
    }

    /**
     * PHÁT MÃ ĐẢO GIÓ (SWING VANE TOGGLE)
     */
    public static int[] buildSwingPattern(BrandData brand, CodeProfile profile) {
        if ("funiki".equals(brand.key) || profile.proto.startsWith("COOLIX")) {
            return encodeCoolixMsb((byte) 0xB2, (byte) 0x01, (byte) 0xFE);
        }
        if (profile.proto.startsWith("MIDEA")) {
            byte b5 = (byte) 0xA2;
            byte b4 = (byte) 0x01;
            byte b3 = (byte) 0xFF;
            byte b2 = (byte) 0xFF;
            byte b1 = (byte) 0xFF;
            byte b0 = (byte) 0x7C;
            return encodeMidea48(b5, b4, b3, b2, b1, b0);
        }
        return encodeCoolixMsb((byte) 0xB2, (byte) 0x01, (byte) 0xFE);
    }

    /**
     * LỆNH HẸN GIỜ TẮT:
     * Dòng Funiki Coolix sử dụng khung sóng Coolix-48 chính xác
     */
    public static int[] buildTimerPattern(BrandData brand, CodeProfile profile, int temp, int deltaMinutes) {
        if ("funiki".equals(brand.key) || profile.proto.startsWith("COOLIX")) {
            return buildCoolixOffTimerPattern(deltaMinutes);
        }

        // Midea RG10 (dòng khác)
        int hours = deltaMinutes / 60;
        if (hours < 1) hours = 1;
        if (hours > 24) hours = 24;
        int halfhours = hours * 2;
        byte b5 = (byte) 0xA1;
        byte b4 = (byte) 0xA0;
        byte b3 = (byte) (0x40 | ((temp - 17) & 0x0F));
        byte b2 = (byte) (0x80 | (((halfhours - 1) & 0x3F) << 1) | 0x01);
        byte b1 = (byte) 0xFF;
        byte b0 = calcMideaChecksum(b5, b4, b3, b2, b1);
        return encodeMidea48(b5, b4, b3, b2, b1, b0);
    }

    /**
     * LỆNH HỦY HẸN GIỜ (CANCEL TIMER)
     */
    public static int[] buildCancelTimerPattern(BrandData brand, CodeProfile profile) {
        if ("funiki".equals(brand.key) || profile.proto.startsWith("COOLIX")) {
            return encodeCoolixMsb((byte) 0xB2, (byte) 0xC5, (byte) 0x3A);
        }
        if (profile.proto.startsWith("MIDEA")) {
            return encodeMidea48((byte) 0xA1, (byte) 0xA0, (byte) 0x49, (byte) 0xFF, (byte) 0xFF, (byte) 0x67);
        }
        return encodeCoolixMsb((byte) 0xB2, (byte) 0xC5, (byte) 0x3A);
    }

    /**
     * CÔNG THỨC MÃ HÓA HẸN GIỜ TẮT CHUẨN COOLIX-48 TỪ 0.5H ĐẾN 24H
     */
    public static int[] buildCoolixOffTimerPattern(int deltaMinutes) {
        int halfhours = (int) Math.round(deltaMinutes / 30.0);
        if (halfhours < 1) halfhours = 1;
        if (halfhours > 48) halfhours = 48;

        int b3, b2, b1, b0;
        if (halfhours <= 16) { // 0.5h đến 8.0h
            int idx = halfhours - 2;
            if (idx < 0) idx = 0;
            b3 = (0xA3 + idx * 2) & 0xFF;
            b2 = 0xFF ^ b3;
            b1 = 0x6C;
            b0 = 0x7F;
        } else if (halfhours <= 32) { // 8.5h đến 16.0h
            int idx = halfhours - 17;
            b3 = (0xA1 + idx * 2) & 0xFF;
            b2 = 0xFF ^ b3;
            b1 = 0x6D;
            b0 = 0x7F;
        } else { // 16.5h đến 24.0h
            int idx = halfhours - 33;
            b3 = (0xA1 + idx * 2) & 0xFF;
            b2 = 0xFF ^ b3;
            b1 = 0x6E;
            b0 = 0x7F;
        }

        byte[] frame = new byte[]{(byte) 0xB2, (byte) 0x4D, (byte) b3, (byte) b2, (byte) b1, (byte) b0};
        return encodeCoolix48(frame);
    }

    /**
     * ENCODER GIAO THỨC COOLIX-48 (48 BIT MSB-FIRST, 2 FRAME)
     */
    public static int[] encodeCoolix48(byte[] frame) {
        List<Integer> pulses = new ArrayList<>(200);

        for (int repeat = 0; repeat < 2; repeat++) {
            // Header
            pulses.add(4690);
            pulses.add(4420);

            for (byte b : frame) {
                encodeByteMsb(pulses, b, 550, 1650, 550);
            }

            pulses.add(550);
            pulses.add(5240);
        }

        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) {
            result[i] = pulses.get(i);
        }
        return result;
    }

    // =========================================================================
    // ENCODER GIAO THỨC MIDEA 48-BIT (200 XUNG)
    // =========================================================================
    public static int[] encodeMidea48(byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
        List<Integer> pulses = new ArrayList<>(200);
        byte[] frame1 = new byte[]{b5, b4, b3, b2, b1, b0};

        // --- KHUNG 1 ---
        pulses.add(4400);
        pulses.add(4400);
        for (byte b : frame1) {
            encodeByteMsb(pulses, b, 540, 1620, 540);
        }
        pulses.add(540);
        pulses.add(5100);

        // --- KHUNG 2 (ĐẢO BITWISE KHUNG 1) ---
        pulses.add(4400);
        pulses.add(4400);
        for (byte b : frame1) {
            encodeByteMsb(pulses, (byte) (~b), 540, 1620, 540);
        }
        pulses.add(540);
        pulses.add(4000);

        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) {
            result[i] = pulses.get(i);
        }
        return result;
    }

    public static byte calcMideaChecksum(byte b5, byte b4, byte b3, byte b2, byte b1) {
        byte[] arr = new byte[]{b5, b4, b3, b2, b1};
        int revSum = 0;
        for (byte b : arr) {
            revSum += reverseBits8(b & 0xFF);
        }
        revSum &= 0xFF;
        int chk = (256 - revSum) & 0xFF;
        return (byte) reverseBits8(chk);
    }

    private static int reverseBits8(int x) {
        int res = 0;
        for (int i = 0; i < 8; i++) {
            if (((x >> i) & 1) == 1) {
                res |= (1 << (7 - i));
            }
        }
        return res;
    }

    // =========================================================================
    // ENCODER GIAO THỨC COOLIX 24-BIT (MSB-FIRST)
    // =========================================================================
    public static int[] encodeCoolixMsb(byte b0, byte b1, byte b2) {
        List<Integer> pulses = new ArrayList<>(160);
        byte[] bytes = new byte[]{b0, b1, b2};

        for (int repeat = 0; repeat < 2; repeat++) {
            pulses.add(4690);
            pulses.add(4420);

            for (byte b : bytes) {
                encodeByteMsb(pulses, b, 550, 1650, 550);
                encodeByteMsb(pulses, (byte) (~b), 550, 1650, 550);
            }

            pulses.add(550);
            pulses.add(5240);
        }

        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) {
            result[i] = pulses.get(i);
        }
        return result;
    }

    private static void encodeByteMsb(List<Integer> pulses, byte b, int mark, int space1, int space0) {
        for (int i = 7; i >= 0; i--) {
            int bit = (b >> i) & 1;
            pulses.add(mark);
            pulses.add(bit == 1 ? space1 : space0);
        }
    }

    private static int[] encodeAuxMsb() {
        List<Integer> pulses = new ArrayList<>();
        pulses.add(9000);
        pulses.add(4500);
        byte[] frame = new byte[]{(byte) 0xC3, 0, 0, 0, 0, 0x20, 0, 0, 0, 0, 0, 0, (byte) 0xE3};
        for (byte b : frame) {
            encodeByteMsb(pulses, b, 560, 1650, 560);
        }
        pulses.add(560);
        pulses.add(10000);
        pulses.add(9000);
        pulses.add(4500);
        for (byte b : frame) {
            encodeByteMsb(pulses, b, 560, 1650, 560);
        }
        pulses.add(560);
        pulses.add(5000);
        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) result[i] = pulses.get(i);
        return result;
    }

    private static int[] encodeGreeMsb() {
        List<Integer> pulses = new ArrayList<>();
        pulses.add(9000);
        pulses.add(4500);
        byte[] gree = new byte[]{0x09, 0x08, 0x20, 0x50, 0x02};
        for (byte b : gree) {
            encodeByteMsb(pulses, b, 600, 1600, 600);
        }
        pulses.add(600);
        pulses.add(20000);
        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) result[i] = pulses.get(i);
        return result;
    }

    private static int[] buildPanasonicPattern() {
        List<Integer> pulses = new ArrayList<>();
        pulses.add(3500);
        pulses.add(1750);
        byte[] lead = new byte[]{0x02, 0x20, (byte) 0xE0, 0x04, 0x00, 0x00, 0x00, 0x06};
        for (byte b : lead) {
            for (int i = 0; i < 8; i++) {
                int bit = (b >> i) & 1;
                pulses.add(440);
                pulses.add(bit == 1 ? 1300 : 430);
            }
        }
        pulses.add(440);
        pulses.add(10000);
        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) result[i] = pulses.get(i);
        return result;
    }

    private static int[] buildDaikinPattern() {
        List<Integer> pulses = new ArrayList<>();
        pulses.add(3500);
        pulses.add(1750);
        byte[] daikin = new byte[]{0x11, (byte) 0xDA, 0x27, 0x00, (byte) 0xC5, 0x00, 0x00, (byte) 0xD7};
        for (byte b : daikin) {
            for (int i = 0; i < 8; i++) {
                int bit = (b >> i) & 1;
                pulses.add(440);
                pulses.add(bit == 1 ? 1300 : 430);
            }
        }
        pulses.add(440);
        pulses.add(10000);
        int[] result = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) result[i] = pulses.get(i);
        return result;
    }
}
