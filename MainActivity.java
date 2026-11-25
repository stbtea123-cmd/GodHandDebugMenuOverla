package com.example.godhandoverlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(22), dp(28), dp(22), dp(22));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(0xFF101010);

        TextView title = new TextView(this);
        title.setText("GOD HAND DEBUG MENU");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        layout.addView(title, new LinearLayout.LayoutParams(-1, dp(64)));

        TextView info = new TextView(this);
        info.setText("Overlay demo untuk NetherSX2\nMenu ini hanya mengubah tampilan ON/OFF lokal. Belum mengaktifkan cheat di dalam game.");
        info.setTextColor(0xFFCCCCCC);
        info.setTextSize(14);
        info.setGravity(Gravity.CENTER);
        layout.addView(info, new LinearLayout.LayoutParams(-1, dp(100)));

        Button permission = new Button(this);
        permission.setText("1. Izinkan tampil di atas aplikasi lain");
        permission.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        layout.addView(permission, new LinearLayout.LayoutParams(-1, dp(52)));

        Button start = new Button(this);
        start.setText("2. Jalankan menu overlay");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                permission.performClick();
                return;
            }
            Intent service = new Intent(this, OverlayService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
            else startService(service);
            finish();
        });
        layout.addView(start, new LinearLayout.LayoutParams(-1, dp(52)));

        Button stop = new Button(this);
        stop.setText("Hentikan overlay");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
        layout.addView(stop, new LinearLayout.LayoutParams(-1, dp(52)));

        setContentView(layout);
    }
}
