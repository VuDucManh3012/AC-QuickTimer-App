package com.xiaomi.acquicktimer;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

public class IrTransmitter {
    private static final String TAG = "IrTransmitter";
    private final ConsumerIrManager irManager;
    private final Context context;

    public IrTransmitter(Context context) {
        this.context = context;
        this.irManager = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
    }

    public boolean hasIrEmitter() {
        return irManager != null && irManager.hasIrEmitter();
    }

    public boolean transmit(int carrierFrequency, int[] pattern) {
        if (!hasIrEmitter()) {
            Log.e(TAG, "Thiết bị không hỗ trợ cổng hồng ngoại IR Emitter!");
            return false;
        }

        try {
            // Rung phản hồi haptic để người dùng biết máy đã phát xung
            triggerVibrate();

            // Phát tín hiệu sóng mang 38kHz ra mắt hồng ngoại Redmi
            irManager.transmit(carrierFrequency, pattern);
            Log.d(TAG, "Phát IR thành công: " + carrierFrequency + "Hz, độ dài: " + pattern.length);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi gọi transmit()", e);
            return false;
        }
    }

    private void triggerVibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    vm.getDefaultVibrator().vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    v.vibrate(40);
                }
            }
        } catch (Exception ignored) {}
    }
}
