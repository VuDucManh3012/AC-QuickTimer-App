package com.xiaomi.acquicktimer;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class SoundFeedback {
    private static ToneGenerator toneGenerator;

    static {
        try {
            // Mức âm lượng tối đa 100% trên luồng SYSTEM/ALARM
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception ignored) {}
    }

    public static void playClick(Context context) {
        // 1. Âm thanh bíp to rõ ràng
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 60);
            }
        } catch (Exception ignored) {}

        // 2. Rung mạnh 100ms
        vibrate(context, 100);
    }

    public static void playScanPulse(Context context) {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 80);
            }
        } catch (Exception ignored) {}
        vibrate(context, 120);
    }

    public static void playSuccess(Context context) {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 200);
            }
        } catch (Exception ignored) {}
        vibrate(context, 250);
    }

    private static void vibrate(Context context, long millis) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    vm.getDefaultVibrator().vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    v.vibrate(millis);
                }
            }
        } catch (Exception ignored) {}
    }
}
