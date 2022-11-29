package com.artuok.appwork;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.artuok.appwork.db.DbHelper;
import com.artuok.appwork.services.AlarmWorkManager;

import java.util.Calendar;

public class InActivity extends AppCompatActivity {

    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";
    public static final String CHANNEL_ID_3 = "CHANNEL_3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppWork);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Preferences();

        createNotificationChannel();
        restauredAlarm();
        setAlarmSchedule();

        new Handler().postDelayed(this::loadMain, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void loadMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    void Preferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean b = sharedPreferences.getBoolean("DarkMode", false);

        if (b) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    void createNotificationChannel() {
        NotificationChannel notificationChannel1 = new NotificationChannel(CHANNEL_ID_1, "NOTES", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel1.setDescription("Channel for remembers");
        NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_2, "Homework", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel2.setDescription("Channel for remember when you need to do homework");
        NotificationChannel notificationChannel3 = new NotificationChannel(CHANNEL_ID_3, "Alarm", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel3.setDescription("Alarm to do homework");

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(notificationChannel2);
        manager.createNotificationChannel(notificationChannel1);
        manager.createNotificationChannel(notificationChannel3);
    }

    void cancelAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notify = new Intent(this, AlarmWorkManager.class)
                .setAction(AlarmWorkManager.ACTION_TIME_TO_DO_HOMEWORK);
        PendingIntent pendingNotify = PendingIntent.getBroadcast(
                this,
                0, notify,
                0);
        manager.cancel(pendingNotify);
    }

    void setAlarm(int hour, int minute, boolean alarm) {
        final Calendar c = Calendar.getInstance();
        long rest = 0;
        int hr = 0;
        if (c.get(Calendar.HOUR_OF_DAY) >= hour) {
            hr = 24 + hour - c.get(Calendar.HOUR_OF_DAY);
        } else {
            hr = hour - c.get(Calendar.HOUR_OF_DAY);
        }

        int mr = minute - c.get(Calendar.MINUTE);

        rest = (hr * 60L * 60L * 1000L) + (mr * 60L * 1000L);

        Calendar a = Calendar.getInstance();
        rest += a.getTimeInMillis();

        a.setTimeInMillis(rest);


        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        long diff = rest - Calendar.getInstance().getTimeInMillis();
        int days = (int) (diff / 1000 / 60 / 60 / 24);
        int hou = (int) (diff / 1000 / 60 / 60 % 24);
        int min = (int) (diff / 1000 / 60 % 60);

        Log.d("catto", "faltan " + days + "d " + hou + "h " + min + "m ");

        Intent notify = new Intent(this, AlarmWorkManager.class)
                .setAction(AlarmWorkManager.ACTION_TIME_TO_DO_HOMEWORK);
        notify.putExtra("time", rest);
        if (alarm) {
            notify.putExtra("alarm", 1);
        }
        PendingIntent pendingNotify = PendingIntent.getBroadcast(
                this,
                0, notify,
                PendingIntent.FLAG_UPDATE_CURRENT);

        manager.cancel(pendingNotify);
        manager.setExact(AlarmManager.RTC_WAKEUP, rest, pendingNotify);
    }

    void restauredAlarm() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean as = sharedPreferences.getBoolean("AlarmSet", false);
        int h = Integer.parseInt(sharedPreferences.getString("timeTDH", "11:00").split(":")[0]);
        int m = Integer.parseInt(sharedPreferences.getString("timeTDH", "11:00").split(":")[1]);
        setAlarm(h, m, as);
    }

    void setAlarmSchedule() {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor v = db.rawQuery("SELECT * FROM " + DbHelper.t_event + " ORDER BY day_of_week ASC, time ASC", null);
        Calendar c = Calendar.getInstance();
        long time = 0;
        long duration = 0;
        int day = -1;
        String name = "";
        long hour = (60 * 60 * c.get(Calendar.HOUR_OF_DAY)) + (60 * c.get(Calendar.MINUTE));
        int dow = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (v.moveToFirst()) {
            do {
                if (v.getLong(3) > (hour + (60 * 5)) && dow == v.getInt(2)) {
                    time = v.getLong(3) * 1000;
                    time = time - (hour * 1000);
                    duration = v.getLong(4) * 1000;
                    name = v.getString(1);
                    break;
                } else if (dow < v.getInt(2)) {
                    time = v.getLong(3) * 1000;
                    day = v.getInt(2);
                    int r = (day + 1) - (dow + 1);
                    time = (r * 86400000L) + (time) - (hour * 1000);
                    duration = v.getLong(4) * 1000;
                    name = v.getString(1);
                    break;
                }
            } while (v.moveToNext());
            if (!(!name.equals("") && time != 0 && duration != 0)) {
                v.moveToFirst();
                do {
                    time = v.getLong(3) * 1000;
                    day = v.getInt(2);
                    int r = 7 - (dow + 1) + (day + 1);
                    time = (r * 86400000L) + (time) - (hour * 1000);
                    duration = v.getLong(4) * 1000;
                    name = v.getString(1);
                    if (duration != 0) {
                        break;
                    }
                } while (v.moveToNext());
            }
        }
        if (!name.equals("") && time != 0 && duration != 0) {
            setNotify(name, time, duration);
        }
        v.close();
    }

    void setNotify(String name, long diff, long duration) {

        long start = Calendar.getInstance().getTimeInMillis() + diff;

        Intent notify = new Intent(this, AlarmWorkManager.class)
                .setAction(AlarmWorkManager.ACTION_EVENT);

        notify.putExtra("name", name);
        notify.putExtra("time", start);
        notify.putExtra("duration", duration);
        PendingIntent pendingNotify = PendingIntent.getBroadcast(
                this,
                1, notify,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingNotify);
        manager.setExact(AlarmManager.RTC_WAKEUP, start - (60 * 5 * 1000), pendingNotify);
    }
}