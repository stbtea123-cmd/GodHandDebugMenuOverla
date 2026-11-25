package com.example.godhandoverlay;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.view.*;
import android.widget.*;

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble, panel;
    private WindowManager.LayoutParams bubbleParams, panelParams;
    private final String[] cheats = {
        "INFINITE HEALTH", "INFINITE GOD HAND", "ONE HIT KILL",
        "INFINITE GOLD", "INFINITE ORBS", "ALL TECHNIQUES", "ALL ROULETTE"
    };
    private final boolean[] states = new boolean[cheats.length];
    private int selected = 0;
    private TextView[] rows;

    private int dp(float v) { return (int)(getResources().getDisplayMetrics().density * v + 0.5f); }
    private int overlayType() {
        return Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    @Override public void onCreate() {
        super.onCreate();
        createNotification();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        createBubble();
        createPanel();
    }

    private void createNotification() {
        String channel = "gh_overlay";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(channel, "God Hand Overlay", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, channel) : new Notification.Builder(this);
        b.setContentTitle("God Hand Debug Menu")
         .setContentText("Overlay berjalan. Ketuk ikon GH untuk membuka menu.")
         .setSmallIcon(android.R.drawable.ic_menu_view)
         .setOngoing(true);
        startForeground(41, b.build());
    }

    private void createBubble() {
        TextView t = new TextView(this);
        t.setText("GH");
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(15);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setBackgroundColor(0xFF8B1111);
        bubble = t;
        bubbleParams = new WindowManager.LayoutParams(dp(54), dp(54), overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(18); bubbleParams.y = dp(180);
        t.setOnClickListener(v -> togglePanel());
        t.setOnTouchListener(new View.OnTouchListener() {
            float sx, sy; int ox, oy; boolean moved;
            public boolean onTouch(View v, android.view.MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    sx=e.getRawX(); sy=e.getRawY(); ox=bubbleParams.x; oy=bubbleParams.y; moved=false; return false;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    float dx=e.getRawX()-sx, dy=e.getRawY()-sy;
                    if (Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5)) moved=true;
                    if (moved) { bubbleParams.x=ox+(int)dx; bubbleParams.y=oy+(int)dy; wm.updateViewLayout(bubble,bubbleParams); }
                    return moved;
                }
                return false;
            }
        });
        wm.addView(bubble, bubbleParams);
    }

    private TextView text(String s, int size, int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.MONOSPACE);
        return t;
    }

    private void createPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackgroundColor(0xF0000000);
        box.addView(text("GOD HAND DEBUG MENU", 16, 0xFFFFFFFF));
        box.addView(text("SLUS-21503  |  OVERLAY DEMO", 10, 0xFFAAAAAA));
        View line = new View(this); line.setBackgroundColor(0xFF777777);
        box.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));

        rows = new TextView[cheats.length];
        for (int i=0;i<cheats.length;i++) {
            final int index=i;
            TextView row=text("", 12, 0xFFFFFFFF);
            row.setPadding(dp(4), dp(7), dp(4), dp(7));
            row.setOnClickListener(v -> { selected=index; states[index]=!states[index]; refresh(); });
            rows[i]=row; box.addView(row);
        }
        TextView note=text("Ketuk baris untuk pilih/toggle\nX = tutup menu | GH = buka lagi", 10, 0xFFBBBBBB);
        box.addView(note);
        panel=box;
        panelParams = new WindowManager.LayoutParams(dp(280), -2, overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        panelParams.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        panelParams.y=dp(90);
        refresh();
    }

    private void refresh() {
        if (rows==null) return;
        for (int i=0;i<rows.length;i++) {
            rows[i].setText((i==selected ? "> " : "  ") + cheats[i] + "   [" + (states[i]?"ON":"OFF") + "]");
            rows[i].setBackgroundColor(i==selected ? 0xFF303030 : 0x00000000);
            rows[i].setTextColor(states[i] ? 0xFF75E69A : 0xFFFFFFFF);
        }
    }

    private void togglePanel() {
        if (panel.getParent()==null) wm.addView(panel,panelParams);
        else wm.removeView(panel);
    }

    @Override public int onStartCommand(Intent i, int flags, int id) { return START_STICKY; }
    @Override public void onDestroy() {
        try { if (panel!=null && panel.getParent()!=null) wm.removeView(panel); } catch(Exception ignored) {}
        try { if (bubble!=null && bubble.getParent()!=null) wm.removeView(bubble); } catch(Exception ignored) {}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
