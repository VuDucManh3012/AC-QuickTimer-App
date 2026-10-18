package com.xiaomi.acquicktimer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;

public class QuickTimerWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_TRIGGER_TIMER = "com.xiaomi.acquicktimer.ACTION_TRIGGER_TIMER";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent intent = new Intent(context, QuickTimerWidgetProvider.class);
            intent.setAction(ACTION_TRIGGER_TIMER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_TRIGGER_TIMER.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("ac_prefs", Context.MODE_PRIVATE);
            String brandKey = prefs.getString("brand_key", "funiki");
            int codeId = prefs.getInt("code_id", 1);
            int temp = prefs.getInt("temp", 26);

            AcProtocolDatabase.BrandData brand = AcProtocolDatabase.BRANDS.get(brandKey);
            if (brand == null) brand = AcProtocolDatabase.BRANDS.get("funiki");

            AcProtocolDatabase.CodeProfile code = brand.codes.get(0);
            for (AcProtocolDatabase.CodeProfile c : brand.codes) {
                if (c.id == codeId) {
                    code = c;
                    break;
                }
            }

            // Tính số phút đến 07:30 sáng
            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, 7);
            target.set(Calendar.MINUTE, 30);
            target.set(Calendar.SECOND, 0);

            if (target.before(now) || target.equals(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1);
            }

            long diffMillis = target.getTimeInMillis() - now.getTimeInMillis();
            int deltaMinutes = (int) (diffMillis / (1000 * 60));

            int fanSpeed = prefs.getInt("fan_speed", 0);
            IrTransmitter transmitter = new IrTransmitter(context);

            // BƯỚC 1: Phát lệnh Bật máy
            int[] powerOnPattern = AcProtocolDatabase.buildPowerOnPattern(brand, code, temp, fanSpeed);
            boolean ok1 = transmitter.transmit(brand.carrier, powerOnPattern);

            // Delay 350ms
            try {
                Thread.sleep(350);
            } catch (InterruptedException ignored) {}

            // BƯỚC 2: Phát lệnh Hẹn Giờ Tắt (Coolix-48)
            int[] timerPattern = AcProtocolDatabase.buildTimerPattern(brand, code, temp, deltaMinutes);
            boolean ok2 = transmitter.transmit(brand.carrier, timerPattern);

            int hours = deltaMinutes / 60;
            int mins = deltaMinutes % 60;
            if (ok1 && ok2) {
                Toast.makeText(context, "⚡ [Widget] Đã Bật máy & Hẹn giờ tắt lúc 7:30 sáng (" + hours + "h " + mins + "p nữa)!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "⚠️ Không phát được IR (Kiểm tra cổng hồng ngoại)", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
