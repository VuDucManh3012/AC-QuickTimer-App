package com.xiaomi.acquicktimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private IrTransmitter irTransmitter;
    private SharedPreferences prefs;

    // Tabs
    private TextView tabRoutine, tabRemote, tabPairing, tabWidget;
    private ScrollView viewRoutine, viewPairing, viewWidget;
    private LinearLayout viewRemote;

    // View 1 (Routine)
    private TextView tvActiveBrand, tvActiveCode, tvTargetTime, tvCountdownDuration, tvTempDisplay, tvModeFanDisplay;
    private Button btnSwitchToPairing, btnTriggerTimer, btnTempMinus, btnTempPlus, btnQuickPowerOff, btnQuickCancelTimer;
    private Button btnQuickPowerOn, btnFanSpeed, btnSwing;

    // View 2 (Pairing)
    private LinearLayout brandSelectorTrigger;
    private TextView tvBrandLogoLetter, tvBrandPickerTitle, tvBrandPickerSubtitle;
    private TextView tvPairingBadge, tvPairingCodeTitle;
    private ImageButton btnTestPowerRound;
    private Button btnPairNoNext, btnPairYesConfirm, btnAutoScan;

    // View 4 (Mi Remote)
    private TextView miBtnBack, miHeaderIcon, miTvOffLabel, miTvDisplayTemp, miBadgeMode, miBadgeSpeed, miBadgeSwing;
    private LinearLayout miLayoutOnDisplay;
    private FrameLayout miDisplayArea, miBtnPower;
    private android.widget.ImageView miIvPowerIcon;
    private Button miBtnMode, miBtnSpeed, miBtnDirection, miBtnSwing, miBtnTempDown, miBtnTempClick, miBtnTempUp, miBtnTimer, miBtnSleep, miBtnMore;

    // State
    private String currentSelectedBrandKey = "funiki";
    private int pairingStepIndex = 0;
    private int currentTemp = 26;
    private int currentFanSpeed = 0; // 0: Auto, 1: Thấp, 2: Vừa, 3: Cao
    private static final String[] FAN_SPEED_NAMES = {"Auto", "Thấp", "Vừa", "Cao"};
    private boolean isAutoScanning = false;

    // Mi Remote State
    private boolean isAcOn = false;
    private int miModeIndex = 0;
    private static final String[] MI_MODES = {"COOL", "DRY", "FAN", "AUTO"};
    private boolean isSwingOn = true;

    private final Handler autoScanHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAutoScanning) return;
            executeAutoScanStep();
            autoScanHandler.postDelayed(this, 1500); // 1.5 giây phát 1 mã
        }
    };

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateRemainingDisplay();
            timeHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        irTransmitter = new IrTransmitter(this);
        prefs = getSharedPreferences("ac_prefs", Context.MODE_PRIVATE);

        // Tự động chuyển cấu hình sang Funiki Mã 6 (đã kiểm chứng) nếu chưa thiết lập hoặc đang lưu Panasonic cũ
        if (!prefs.contains("funiki_configured_v3") || "panasonic".equals(prefs.getString("brand_key", ""))) {
            prefs.edit()
                    .putString("brand_key", "funiki")
                    .putInt("code_id", 6)
                    .putBoolean("funiki_configured_v3", true)
                    .apply();
        }

        currentTemp = prefs.getInt("temp", 26);
        currentSelectedBrandKey = prefs.getString("brand_key", "funiki");
        pairingStepIndex = 5; // Mặc định trỏ sẵn vào Mã 6 (Lệnh Bật Máy Coolix)

        initViews();
        setupTabs();
        loadSavedConfig();
        updatePairingUI();
        updateRemainingDisplay();
    }

    private void initViews() {
        tabRoutine = findViewById(R.id.tab_routine);
        tabRemote = findViewById(R.id.tab_remote);
        tabPairing = findViewById(R.id.tab_pairing);
        tabWidget = findViewById(R.id.tab_widget);

        viewRoutine = findViewById(R.id.view_routine);
        viewRemote = findViewById(R.id.view_remote);
        viewPairing = findViewById(R.id.view_pairing);
        viewWidget = findViewById(R.id.view_widget);

        // View 4 (Mi Remote)
        miBtnBack = findViewById(R.id.mi_btn_back);
        miHeaderIcon = findViewById(R.id.mi_header_icon);
        miDisplayArea = findViewById(R.id.mi_display_area);
        miTvOffLabel = findViewById(R.id.mi_tv_off_label);
        miLayoutOnDisplay = findViewById(R.id.mi_layout_on_display);
        miTvDisplayTemp = findViewById(R.id.mi_tv_display_temp);
        miBadgeMode = findViewById(R.id.mi_badge_mode);
        miBadgeSpeed = findViewById(R.id.mi_badge_speed);
        miBadgeSwing = findViewById(R.id.mi_badge_swing);
        miBtnPower = findViewById(R.id.mi_btn_power);
        miIvPowerIcon = findViewById(R.id.mi_iv_power_icon);
        miBtnMode = findViewById(R.id.mi_btn_mode);
        miBtnSpeed = findViewById(R.id.mi_btn_speed);
        miBtnDirection = findViewById(R.id.mi_btn_direction);
        miBtnSwing = findViewById(R.id.mi_btn_swing);
        miBtnTempDown = findViewById(R.id.mi_btn_temp_down);
        miBtnTempClick = findViewById(R.id.mi_btn_temp_click);
        miBtnTempUp = findViewById(R.id.mi_btn_temp_up);
        miBtnTimer = findViewById(R.id.mi_btn_timer);
        miBtnSleep = findViewById(R.id.mi_btn_sleep);
        miBtnMore = findViewById(R.id.mi_btn_more);

        // View 1
        tvActiveBrand = findViewById(R.id.tv_active_brand);
        tvActiveCode = findViewById(R.id.tv_active_code);
        tvTargetTime = findViewById(R.id.tv_target_time);
        tvCountdownDuration = findViewById(R.id.tv_countdown_duration);
        tvTempDisplay = findViewById(R.id.tv_temp_display);
        tvModeFanDisplay = findViewById(R.id.tv_mode_fan_display);
        btnSwitchToPairing = findViewById(R.id.btn_switch_to_pairing);
        btnTriggerTimer = findViewById(R.id.btn_trigger_timer);
        btnTempMinus = findViewById(R.id.btn_temp_minus);
        btnTempPlus = findViewById(R.id.btn_temp_plus);
        btnQuickPowerOn = findViewById(R.id.btn_quick_power_on);
        btnQuickPowerOff = findViewById(R.id.btn_quick_power_off);
        btnFanSpeed = findViewById(R.id.btn_fan_speed);
        btnSwing = findViewById(R.id.btn_swing);
        btnQuickCancelTimer = findViewById(R.id.btn_quick_cancel_timer);

        // View 2
        brandSelectorTrigger = findViewById(R.id.brand_selector_trigger);
        tvBrandLogoLetter = findViewById(R.id.tv_brand_logo_letter);
        tvBrandPickerTitle = findViewById(R.id.tv_brand_picker_title);
        tvBrandPickerSubtitle = findViewById(R.id.tv_brand_picker_subtitle);
        tvPairingBadge = findViewById(R.id.tv_pairing_badge);
        tvPairingCodeTitle = findViewById(R.id.tv_pairing_code_title);
        btnTestPowerRound = findViewById(R.id.btn_test_power_round);
        btnPairNoNext = findViewById(R.id.btn_pair_no_next);
        btnPairYesConfirm = findViewById(R.id.btn_pair_yes_confirm);
        btnAutoScan = findViewById(R.id.btn_auto_scan);

        tvTempDisplay.setText(currentTemp + "°C");

        // Logic Nút Dò Tự Động (Auto-Scan)
        btnAutoScan.setOnClickListener(v -> toggleAutoScan());

        // Action: 1-Click Trigger
        btnTriggerTimer.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            triggerOneClickRoutine();
        });

        btnSwitchToPairing.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            switchTab(1);
        });

        // Temp
        btnTempMinus.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            if (currentTemp > 16) {
                currentTemp--;
                tvTempDisplay.setText(currentTemp + "°C");
                prefs.edit().putInt("temp", currentTemp).apply();
            }
        });

        btnTempPlus.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            if (currentTemp < 30) {
                currentTemp++;
                tvTempDisplay.setText(currentTemp + "°C");
                prefs.edit().putInt("temp", currentTemp).apply();
            }
        });

        // Quick Controls
        btnQuickPowerOn.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
            irTransmitter.transmit(brand.carrier, pattern);
            Toast.makeText(this, "Đã gửi lệnh Bật điều hòa (" + currentTemp + "°C - Quạt " + FAN_SPEED_NAMES[currentFanSpeed] + ")", Toast.LENGTH_SHORT).show();
        });

        btnQuickPowerOff.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildPowerOffPattern(brand, code);
            irTransmitter.transmit(brand.carrier, pattern);
            Toast.makeText(this, "Đã gửi lệnh Tắt điều hòa", Toast.LENGTH_SHORT).show();
        });

        btnFanSpeed.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            currentFanSpeed = (currentFanSpeed + 1) % 4;
            prefs.edit().putInt("fan_speed", currentFanSpeed).apply();
            updateFanSpeedUI();

            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
            irTransmitter.transmit(brand.carrier, pattern);

            Toast.makeText(this, "Đã đổi tốc độ gió: " + FAN_SPEED_NAMES[currentFanSpeed], Toast.LENGTH_SHORT).show();
        });

        btnSwing.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildSwingPattern(brand, code);
            irTransmitter.transmit(brand.carrier, pattern);
            Toast.makeText(this, "Đã gửi lệnh Đảo Gió (Swing)", Toast.LENGTH_SHORT).show();
        });

        btnQuickCancelTimer.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildCancelTimerPattern(brand, code);
            irTransmitter.transmit(brand.carrier, pattern);
            Toast.makeText(this, "⏰ Đã gửi lệnh Hủy Hẹn Giờ", Toast.LENGTH_SHORT).show();
        });

        // Brand Picker Popup
        brandSelectorTrigger.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            showBrandPickerDialog();
        });

        // Pairing Actions
        btnTestPowerRound.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(currentSelectedBrandKey);
            if (brand != null && pairingStepIndex < brand.codes.size()) {
                AcProtocolDatabase.CodeProfile code = brand.codes.get(pairingStepIndex);
                int[] pattern = AcProtocolDatabase.buildPowerTogglePattern(brand, code);
                irTransmitter.transmit(brand.carrier, pattern);
                Toast.makeText(this, "📡 Đang phát mã: " + code.name, Toast.LENGTH_SHORT).show();
            }
        });

        btnPairNoNext.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(currentSelectedBrandKey);
            if (brand != null) {
                if (pairingStepIndex < brand.codes.size() - 1) {
                    pairingStepIndex++;
                } else {
                    pairingStepIndex = 0;
                }
                updatePairingUI();
                Toast.makeText(this, "🔄 Chuyển sang mã số " + (pairingStepIndex + 1), Toast.LENGTH_SHORT).show();
            }
        });

        btnPairYesConfirm.setOnClickListener(v -> {
            SoundFeedback.playSuccess(this);
            AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(currentSelectedBrandKey);
            if (brand != null && pairingStepIndex < brand.codes.size()) {
                AcProtocolDatabase.CodeProfile code = brand.codes.get(pairingStepIndex);
                prefs.edit()
                        .putString("brand_key", brand.key)
                        .putInt("code_id", code.id)
                        .apply();
                loadSavedConfig();
                Toast.makeText(this, "✅ Đã lưu cấu hình: " + brand.name + "\n" + code.name, Toast.LENGTH_LONG).show();
                switchTab(0);
            }
        });

        // ==========================================
        // MI REMOTE BUTTON LISTENERS
        // ==========================================
        miBtnPower.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            isAcOn = !isAcOn;
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);

            if (isAcOn) {
                int[] pattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
                irTransmitter.transmit(brand.carrier, pattern);
                Toast.makeText(this, "🟢 Đã BẬT điều hòa (Mã 6 Coolix)", Toast.LENGTH_SHORT).show();
            } else {
                int[] pattern = AcProtocolDatabase.buildPowerOffPattern(brand, code);
                irTransmitter.transmit(brand.carrier, pattern);
                Toast.makeText(this, "🛑 Đã TẮT điều hòa (Mã 5 Coolix)", Toast.LENGTH_SHORT).show();
            }
            updateMiRemoteUI();
        });

        miBtnMode.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            if (!isAcOn) {
                Toast.makeText(this, "⚠️ Vui lòng bật nguồn trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            miModeIndex = (miModeIndex + 1) % MI_MODES.length;
            updateMiRemoteUI();
            Toast.makeText(this, "❄️ Chế độ: " + MI_MODES[miModeIndex], Toast.LENGTH_SHORT).show();
        });

        miBtnSpeed.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            if (!isAcOn) {
                Toast.makeText(this, "⚠️ Vui lòng bật nguồn trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            currentFanSpeed = (currentFanSpeed + 1) % 4;
            prefs.edit().putInt("fan_speed", currentFanSpeed).apply();
            updateFanSpeedUI();

            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
            irTransmitter.transmit(brand.carrier, pattern);

            updateMiRemoteUI();
            Toast.makeText(this, "💨 Tốc độ gió: " + FAN_SPEED_NAMES[currentFanSpeed], Toast.LENGTH_SHORT).show();
        });

        miBtnDirection.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildSwingPattern(brand, code);
            irTransmitter.transmit(brand.carrier, pattern);
            Toast.makeText(this, "📐 Đã gửi lệnh chỉnh nấc cánh vẫy gió", Toast.LENGTH_SHORT).show();
        });

        miBtnSwing.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            isSwingOn = !isSwingOn;
            AcProtocolDatabase.BrandData brand = getActiveBrand();
            AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
            int[] pattern = AcProtocolDatabase.buildSwingPattern(brand, code);
            irTransmitter.transmit(brand.carrier, pattern);
            updateMiRemoteUI();
            Toast.makeText(this, "↔ Đảo gió (Swing): " + (isSwingOn ? "BẬT" : "TẮT"), Toast.LENGTH_SHORT).show();
        });

        miBtnTempDown.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            if (!isAcOn) {
                Toast.makeText(this, "⚠️ Vui lòng bật nguồn trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentTemp > 16) {
                currentTemp--;
                prefs.edit().putInt("temp", currentTemp).apply();
                tvTempDisplay.setText(currentTemp + "°C");

                AcProtocolDatabase.BrandData brand = getActiveBrand();
                AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
                int[] pattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
                irTransmitter.transmit(brand.carrier, pattern);

                updateMiRemoteUI();
                Toast.makeText(this, "❄️ Nhiệt độ: " + currentTemp + "°C", Toast.LENGTH_SHORT).show();
            }
        });

        miBtnTempUp.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            if (!isAcOn) {
                Toast.makeText(this, "⚠️ Vui lòng bật nguồn trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentTemp < 30) {
                currentTemp++;
                prefs.edit().putInt("temp", currentTemp).apply();
                tvTempDisplay.setText(currentTemp + "°C");

                AcProtocolDatabase.BrandData brand = getActiveBrand();
                AcProtocolDatabase.CodeProfile code = getActiveCode(brand);
                int[] pattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
                irTransmitter.transmit(brand.carrier, pattern);

                updateMiRemoteUI();
                Toast.makeText(this, "❄️ Nhiệt độ: " + currentTemp + "°C", Toast.LENGTH_SHORT).show();
            }
        });

        miBtnTempClick.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            Toast.makeText(this, "🌡️ Nhiệt độ đang cài đặt: " + currentTemp + "°C", Toast.LENGTH_SHORT).show();
        });

        miBtnTimer.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            triggerOneClickRoutine();
        });

        miBtnSleep.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            Toast.makeText(this, "🌙 Đã gửi lệnh Sleep Mode", Toast.LENGTH_SHORT).show();
        });

        miBtnMore.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            Toast.makeText(this, "⚙️ Tùy chọn cài đặt điều hòa", Toast.LENGTH_SHORT).show();
        });

        miBtnBack.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            switchTab(0);
        });
    }

    private void updateMiRemoteUI() {
        if (miTvOffLabel == null) return;
        if (isAcOn) {
            miTvOffLabel.setVisibility(View.GONE);
            miLayoutOnDisplay.setVisibility(View.VISIBLE);
            miDisplayArea.setBackgroundColor(0xFF182230);
            miIvPowerIcon.setImageResource(R.drawable.ic_power_green);
            miTvDisplayTemp.setText(String.valueOf(currentTemp));
            miBadgeMode.setText(MI_MODES[miModeIndex]);
            miBadgeSpeed.setText("SPEED: " + FAN_SPEED_NAMES[currentFanSpeed].toUpperCase());
            miBadgeSwing.setText(isSwingOn ? "SWING ON" : "SWING OFF");
        } else {
            miTvOffLabel.setVisibility(View.VISIBLE);
            miLayoutOnDisplay.setVisibility(View.GONE);
            miDisplayArea.setBackgroundColor(0xFF181818);
            miIvPowerIcon.setImageResource(R.drawable.ic_power_gray);
        }
    }

    private void toggleAutoScan() {
        if (isAutoScanning) {
            stopAutoScan();
            btnPairYesConfirm.performClick();
        } else {
            startAutoScan();
        }
    }

    private void startAutoScan() {
        isAutoScanning = true;
        SoundFeedback.playSuccess(this);
        btnAutoScan.setText("⏹ DỪNG & CHỌN MÃ NÀY (ĐANG DÒ...)");
        btnAutoScan.setBackgroundResource(R.drawable.btn_orange_round);
        btnAutoScan.setBackgroundTintList(null);
        Toast.makeText(this, "🚀 ĐANG DÒ TỰ ĐỘNG! Chĩa máy vào điều hòa, khi kêu BÍP hãy bấm DỪNG ngay!", Toast.LENGTH_LONG).show();
        autoScanHandler.post(autoScanRunnable);
    }

    private void stopAutoScan() {
        isAutoScanning = false;
        autoScanHandler.removeCallbacks(autoScanRunnable);
        btnAutoScan.setText("▶ BẮT ĐẦU DÒ TỰ ĐỘNG (AUTO-SCAN)");
        btnAutoScan.setBackgroundResource(R.drawable.btn_green_round);
        btnAutoScan.setBackgroundTintList(null);
    }

    private void executeAutoScanStep() {
        AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(currentSelectedBrandKey);
        if (brand == null || brand.codes.isEmpty()) return;

        AcProtocolDatabase.CodeProfile code = brand.codes.get(pairingStepIndex);
        int[] pattern = AcProtocolDatabase.buildPowerTogglePattern(brand, code);
        irTransmitter.transmit(brand.carrier, pattern);

        SoundFeedback.playScanPulse(this);
        updatePairingUI();

        if (pairingStepIndex < brand.codes.size() - 1) {
            pairingStepIndex++;
        } else {
            pairingStepIndex = 0;
            stopAutoScan();
            Toast.makeText(this, "Đã quét hết danh sách mã! Bạn có thể bấm quét lại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTabs() {
        tabRoutine.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            switchTab(0);
        });
        tabRemote.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            switchTab(1);
        });
        tabPairing.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            switchTab(2);
        });
        tabWidget.setOnClickListener(v -> {
            SoundFeedback.playClick(this);
            switchTab(3);
        });
    }

    private void switchTab(int tabIndex) {
        tabRoutine.setBackgroundResource(tabIndex == 0 ? R.drawable.btn_orange_round : android.R.color.transparent);
        tabRoutine.setTextColor(tabIndex == 0 ? 0xFFFFFFFF : 0xFF9CA3AF);

        tabRemote.setBackgroundResource(tabIndex == 1 ? R.drawable.btn_orange_round : android.R.color.transparent);
        tabRemote.setTextColor(tabIndex == 1 ? 0xFFFFFFFF : 0xFF9CA3AF);

        tabPairing.setBackgroundResource(tabIndex == 2 ? R.drawable.btn_orange_round : android.R.color.transparent);
        tabPairing.setTextColor(tabIndex == 2 ? 0xFFFFFFFF : 0xFF9CA3AF);

        tabWidget.setBackgroundResource(tabIndex == 3 ? R.drawable.btn_orange_round : android.R.color.transparent);
        tabWidget.setTextColor(tabIndex == 3 ? 0xFFFFFFFF : 0xFF9CA3AF);

        viewRoutine.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        viewRemote.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        viewPairing.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        viewWidget.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        if (tabIndex == 1) {
            updateMiRemoteUI();
        }
    }

    private void showBrandPickerDialog() {
        final List<AcProtocolDatabase.BrandData> brandList = new ArrayList<>(AcProtocolDatabase.BRANDS.values());
        String[] brandDisplayNames = new String[brandList.size()];
        for (int i = 0; i < brandList.size(); i++) {
            AcProtocolDatabase.BrandData b = brandList.get(i);
            brandDisplayNames[i] = b.name + " (" + b.codes.size() + " bộ mã)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hãng điều hòa");
        builder.setItems(brandDisplayNames, (dialog, which) -> {
            SoundFeedback.playClick(this);
            AcProtocolDatabase.BrandData selected = brandList.get(which);
            currentSelectedBrandKey = selected.key;
            pairingStepIndex = 0;
            updatePairingUI();
            Toast.makeText(this, "Đã chọn: " + selected.name, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void updatePairingUI() {
        AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(currentSelectedBrandKey);
        if (brand == null) brand = AcProtocolDatabase.BRANDS.get("funiki");

        tvBrandLogoLetter.setText(brand.name.substring(0, 1));
        tvBrandPickerTitle.setText(brand.name);
        tvBrandPickerSubtitle.setText(brand.codes.size() + " bộ mã có sẵn");

        if (pairingStepIndex < brand.codes.size()) {
            AcProtocolDatabase.CodeProfile code = brand.codes.get(pairingStepIndex);
            tvPairingBadge.setText("ĐANG THỬ: MÃ " + (pairingStepIndex + 1) + " / " + brand.codes.size());
            tvPairingCodeTitle.setText(code.name);
        }
    }

    private void loadSavedConfig() {
        String savedBrandKey = prefs.getString("brand_key", "funiki");
        int savedCodeId = prefs.getInt("code_id", 1);

        AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(savedBrandKey);
        if (brand == null) brand = AcProtocolDatabase.BRANDS.get("funiki");

        AcProtocolDatabase.CodeProfile code = brand.codes.get(0);
        for (AcProtocolDatabase.CodeProfile c : brand.codes) {
            if (c.id == savedCodeId) {
                code = c;
                break;
            }
        }

        tvActiveBrand.setText(brand.name);
        tvActiveCode.setText("Mã: " + code.name);

        currentFanSpeed = prefs.getInt("fan_speed", 0);
        updateFanSpeedUI();
    }

    private void updateFanSpeedUI() {
        if (btnFanSpeed != null) {
            btnFanSpeed.setText("Gió: " + FAN_SPEED_NAMES[currentFanSpeed]);
        }
        if (tvModeFanDisplay != null) {
            tvModeFanDisplay.setText("COOL / " + FAN_SPEED_NAMES[currentFanSpeed].toUpperCase());
        }
    }

    private void triggerOneClickRoutine() {
        AcProtocolDatabase.BrandData brand = getActiveBrand();
        AcProtocolDatabase.CodeProfile code = getActiveCode(brand);

        int deltaMinutes = calculateDeltaMinutesTo730();
        int hours = deltaMinutes / 60;
        int mins = deltaMinutes % 60;

        // BƯỚC 1: Phát lệnh Bật máy
        int[] powerOnPattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, currentTemp, currentFanSpeed);
        boolean ok1 = irTransmitter.transmit(brand.carrier, powerOnPattern);

        // BƯỚC 2: Sau 350ms, phát lệnh Hẹn Giờ Tắt (Coolix-48)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int[] timerPattern = AcProtocolDatabase.buildTimerPattern(brand, code, currentTemp, deltaMinutes);
            boolean ok2 = irTransmitter.transmit(brand.carrier, timerPattern);
            if (ok2) {
                Toast.makeText(this, "⚡ Đã Bật máy & Hẹn giờ tắt lúc 7:30 sáng (" + hours + "h " + mins + "p nữa)!", Toast.LENGTH_LONG).show();
            }
        }, 350);

        if (!ok1) {
            Toast.makeText(this, "⚠️ Không phát được hồng ngoại! Hãy kiểm tra quyền hoặc cổng IR.", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateDeltaMinutesTo730() {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 7);
        target.set(Calendar.MINUTE, 30);
        target.set(Calendar.SECOND, 0);

        if (target.before(now) || target.equals(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        long diffMillis = target.getTimeInMillis() - now.getTimeInMillis();
        return (int) (diffMillis / (1000 * 60));
    }

    private void updateRemainingDisplay() {
        int deltaMinutes = calculateDeltaMinutesTo730();
        int hours = deltaMinutes / 60;
        int mins = deltaMinutes % 60;
        tvCountdownDuration.setText("Cách hiện tại: " + hours + "h " + mins + "m nữa");
    }

    private AcProtocolDatabase.BrandData getActiveBrand() {
        String savedKey = prefs.getString("brand_key", "funiki");
        AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(savedKey);
        return brand != null ? brand : AcProtocolDatabase.BRANDS.get("funiki");
    }

    private AcProtocolDatabase.CodeProfile getActiveCode(AcProtocolDatabase.BrandData brand) {
        int savedCodeId = prefs.getInt("code_id", 1);
        for (AcProtocolDatabase.CodeProfile c : brand.codes) {
            if (c.id == savedCodeId) return c;
        }
        return brand.codes.get(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeHandler.post(timeRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeRunnable);
    }
}
